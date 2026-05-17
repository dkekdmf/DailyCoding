package com.example.zero.healthcare.service;

import com.example.zero.healthcare.Entity.DiaryFolder;
import com.example.zero.healthcare.Entity.DiaryFolderMember;
import com.example.zero.healthcare.Entity.Invite;
import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.dto.folder.FolderCreateRequest;
import com.example.zero.healthcare.dto.folder.FolderCreateResponse;
import com.example.zero.healthcare.dto.folder.FolderListResponse;
import com.example.zero.healthcare.dto.folder.FolderResponse;
import com.example.zero.healthcare.dto.folder.FolderUpdateRequest;
import com.example.zero.healthcare.dto.folder.InviteAcceptRequest;
import com.example.zero.healthcare.dto.folder.InviteLinkResponse;
import com.example.zero.healthcare.exception.CoreException;
import com.example.zero.healthcare.exception.common.ErrorCode;
import com.example.zero.healthcare.client.AirbridgeTrackingLinkClient;
import com.example.zero.healthcare.repository.DiaryFolderMemberRepository;
import com.example.zero.healthcare.repository.DiaryFolderRepository;
import com.example.zero.healthcare.repository.InviteRepository;
import com.example.zero.healthcare.repository.UserRepository;
import com.example.zero.healthcare.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryFolderService {

    private final DiaryFolderRepository folderRepository;
    private final DiaryFolderMemberRepository memberRepository;
    private final InviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final AirbridgeTrackingLinkClient airbridgeClient;

    // Disallow control chars, newlines, tabs — allow everything else (emoji, space, etc.)
    private static final Pattern INVALID_CHARS = Pattern.compile("[\\p{Cntrl}]");

    private void validateFolderName(String name) {
        if (name == null) throw new CoreException(ErrorCode.INVALID_FOLDER_NAME);
        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > 18) throw new CoreException(ErrorCode.INVALID_FOLDER_NAME);
        if (INVALID_CHARS.matcher(trimmed).find()) throw new CoreException(ErrorCode.INVALID_FOLDER_NAME);
    }

    @Transactional
    public FolderCreateResponse createFolder(Long userId, FolderCreateRequest request) {
        validateFolderName(request.getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new CoreException(ErrorCode.UNAUTHORIZED));
        DiaryFolder folder = DiaryFolder.create(request.getName().trim());
        folderRepository.save(folder);
        DiaryFolderMember member = DiaryFolderMember.join(folder, user);
        memberRepository.save(member);
        return new FolderCreateResponse(folder);
    }

    @Transactional(readOnly = true)
    public FolderListResponse listFolders(Long userId, String cursor, int size, String sort, Boolean shared) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CoreException(ErrorCode.UNAUTHORIZED));
        List<DiaryFolderMember> myMemberships = memberRepository.findActiveFolderMembersByUser(user);

        // cursor filtering
        Long cursorId = (cursor != null && !cursor.isBlank()) ? CursorUtils.decode(cursor) : null;

        // sort
        Comparator<DiaryFolderMember> comparator = "CREATED_AT".equals(sort)
                ? Comparator.comparing(m -> m.getFolder().getCreatedAt(), Comparator.reverseOrder())
                : Comparator.comparing(m -> m.getFolder().getUpdatedAt(), Comparator.reverseOrder());

        List<DiaryFolderMember> sorted = myMemberships.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        // apply cursor
        if (cursorId != null) {
            Long finalCursorId = cursorId;
            int idx = -1;
            for (int i = 0; i < sorted.size(); i++) {
                if (sorted.get(i).getFolder().getId().equals(finalCursorId)) {
                    idx = i;
                    break;
                }
            }
            sorted = idx >= 0 ? sorted.subList(idx + 1, sorted.size()) : sorted;
        }

        // filter shared
        if (shared != null) {
            sorted = sorted.stream().filter(m -> {
                long count = memberRepository.countActiveMembers(m.getFolder());
                return shared ? count >= 2 : count < 2;
            }).collect(Collectors.toList());
        }

        boolean hasNext = sorted.size() > size;
        List<DiaryFolderMember> page = hasNext ? sorted.subList(0, size) : sorted;

        List<FolderResponse> data = page.stream().map(m -> {
            List<DiaryFolderMember> activeMembers = memberRepository.findActiveMembers(m.getFolder());
            return new FolderResponse(m.getFolder(), activeMembers);
        }).collect(Collectors.toList());

        String nextCursor = hasNext ? CursorUtils.encode(page.get(page.size() - 1).getFolder().getId()) : null;
        return new FolderListResponse(data, nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public FolderResponse getFolder(Long userId, Long folderId) {
        DiaryFolder folder = folderRepository.findById(folderId)
                .filter(DiaryFolder::isActive)
                .orElseThrow(() -> new CoreException(ErrorCode.FOLDER_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new CoreException(ErrorCode.UNAUTHORIZED));
        memberRepository.findByFolderAndUser(folder, user)
                .filter(DiaryFolderMember::isActive)
                .orElseThrow(() -> new CoreException(ErrorCode.FORBIDDEN));
        List<DiaryFolderMember> activeMembers = memberRepository.findActiveMembers(folder);
        return new FolderResponse(folder, activeMembers);
    }

    @Transactional
    public FolderResponse updateFolderName(Long userId, Long folderId, FolderUpdateRequest request) {
        validateFolderName(request.getName());
        DiaryFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CoreException(ErrorCode.FOLDER_NOT_FOUND));
        if (!folder.isActive()) throw new CoreException(ErrorCode.FOLDER_CLOSED);
        User user = userRepository.findById(userId).orElseThrow(() -> new CoreException(ErrorCode.UNAUTHORIZED));
        memberRepository.findByFolderAndUser(folder, user)
                .filter(DiaryFolderMember::isActive)
                .orElseThrow(() -> new CoreException(ErrorCode.FORBIDDEN));
        folder.rename(request.getName().trim());
        List<DiaryFolderMember> activeMembers = memberRepository.findActiveMembers(folder);
        return new FolderResponse(folder, activeMembers);
    }

    @Transactional
    public void leaveFolder(Long userId, Long folderId) {
        DiaryFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CoreException(ErrorCode.FOLDER_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new CoreException(ErrorCode.UNAUTHORIZED));
        DiaryFolderMember membership = memberRepository.findByFolderAndUser(folder, user)
                .orElseThrow(() -> new CoreException(ErrorCode.FORBIDDEN));
        if (!membership.isActive()) throw new CoreException(ErrorCode.ALREADY_LEFT);
        membership.leave();
        long remaining = memberRepository.countActiveMembers(folder);
        if (remaining == 0) folder.close();
    }

    @Transactional
    public InviteLinkResponse createInviteLink(Long userId, Long folderId) {
        DiaryFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CoreException(ErrorCode.FOLDER_NOT_FOUND));
        if (!folder.isActive()) throw new CoreException(ErrorCode.FOLDER_CLOSED);
        User user = userRepository.findById(userId).orElseThrow(() -> new CoreException(ErrorCode.UNAUTHORIZED));
        memberRepository.findByFolderAndUser(folder, user)
                .filter(DiaryFolderMember::isActive)
                .orElseThrow(() -> new CoreException(ErrorCode.FORBIDDEN));
        long activeCount = memberRepository.countActiveMembers(folder);
        if (activeCount >= 2) throw new CoreException(ErrorCode.FOLDER_FULL);

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = sha256(rawToken);
        Invite invite = Invite.create(folder, tokenHash);
        inviteRepository.save(invite);

        String encodedName = java.net.URLEncoder.encode(folder.getName(), java.nio.charset.StandardCharsets.UTF_8);
        String deeplink = "ttdev://invite?folderId=" + folderId + "&token=" + rawToken + "&folderName=" + encodedName;
        String trackingLink = airbridgeClient.createTrackingLink(deeplink);
        return new InviteLinkResponse(trackingLink);
    }

    @Transactional
    public FolderResponse acceptInvite(Long userId, InviteAcceptRequest request) {
        String tokenHash = sha256(request.getToken());
        Invite invite = inviteRepository.findByTokenHashAndActiveTrue(tokenHash)
                .orElseThrow(() -> new CoreException(ErrorCode.INVALID_TOKEN));
        DiaryFolder folder = invite.getFolder();
        if (!folder.isActive()) throw new CoreException(ErrorCode.FOLDER_CLOSED);

        User user = userRepository.findById(userId).orElseThrow(() -> new CoreException(ErrorCode.UNAUTHORIZED));

        Optional<DiaryFolderMember> existing = memberRepository.findByFolderAndUser(folder, user);
        if (existing.isPresent() && existing.get().isActive()) {
            throw new CoreException(ErrorCode.ALREADY_MEMBER);
        }

        long activeCount = memberRepository.countActiveMembers(folder);
        if (activeCount >= 2) throw new CoreException(ErrorCode.FOLDER_FULL);

        if (existing.isPresent()) {
            existing.get().rejoin();
        } else {
            memberRepository.save(DiaryFolderMember.join(folder, user));
        }

        invite.deactivate();
        List<DiaryFolderMember> activeMembers = memberRepository.findActiveMembers(folder);
        return new FolderResponse(folder, activeMembers);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.healthcareapp.databinding.ExercisepopupBinding

class WorkoutFinishDialog(private val onConfirm: () -> Unit) : DialogFragment() {

    private var _binding: ExercisepopupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ExercisepopupBinding.inflate(inflater, container, false)

        // 다이얼로그 배경을 투명하게 해야 우리가 만든 곡선 배경이 제대로 보입니다.
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.btnYes.setOnClickListener {
            onConfirm() // '네'를 누르면 오늘 날짜에 이모티콘 박는 로직 실행
            dismiss()
        }

        binding.btnNo.setOnClickListener {
            dismiss() // '아니오'는 그냥 닫기
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
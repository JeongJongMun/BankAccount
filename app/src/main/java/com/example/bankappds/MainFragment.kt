package com.example.bankappds

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bankappds.databinding.FragmentMainBinding
import com.example.bankappds.viewmodel.DataViewModel
import com.google.firebase.firestore.FirebaseFirestore


class MainFragment : Fragment() {

    // 프래그먼트에서 context 사용 가능
    lateinit var mainActivity: MainActivity
    val db : FirebaseFirestore = FirebaseFirestore.getInstance()

    private var binding : FragmentMainBinding? = null
    val viewModel: DataViewModel by activityViewModels()
    private val channelId: String = "MY_CH"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.name.observe(viewLifecycleOwner) {
            binding?.txtName?.text = viewModel.name.value?: "Unknown"
        }
        viewModel.email.observe(viewLifecycleOwner) {
            println(it)
        }

        childFragmentManager.beginTransaction().replace(R.id.frm_fragment, DayCalendarFragment()).commit()

        binding?.imgBtnProfile?.setOnClickListener {
            if (MyApplication.checkAuth()) {
                findNavController().navigate(R.id.action_mainFragment_to_logoutFragment)
            }
            else findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
        }
        createNotificationChannel("MY_CH", "warningChannel", "totalExpense over goalExpense")
        viewModel.totalExpense.observe(viewLifecycleOwner) { // 총 지출이 목표 지출보다 높을 경우 경고 알람
            if (viewModel.goalExpense.value?.toInt() != 0){
                if (it > viewModel.goalExpense.value.toString().toInt()) {
                    displayNotification()
                }
            }
        }
    }

    private var notificationManager: NotificationManager? = null

    fun displayNotification() {
        val notificationId = 45
        // 알람 클릭시 넘어갈 엑티비티 설정
        val mPendingIntent = PendingIntent.getActivity(requireContext(), 0,
            Intent(requireContext(), MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("지출 경고!!")
            .setContentText("총 지출이 목표 지출을 넘어섰습니다!! 총 지출 : ${viewModel.totalExpense.value}, 목표 지출 : ${viewModel.goalExpense.value} ")
            .setContentIntent(mPendingIntent) // PendingIntent 설정
            .build()

        notificationManager?.notify(notificationId, notification)
    }

    fun createNotificationChannel(channelId: String, name: String, channelDescription: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT // set importance
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = channelDescription
            }
            notificationManager = mainActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

}
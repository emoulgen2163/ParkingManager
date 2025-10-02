package com.mycompany.parkingmanager.presentation.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mycompany.parkingmanager.R
import com.mycompany.parkingmanager.domain.utils.AuthenticationManager
import com.mycompany.parkingmanager.presentation.adapters.DashboardAdapter
import com.mycompany.parkingmanager.presentation.ui.activities.LoginActivity
import com.mycompany.parkingmanager.presentation.ui.activities.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    lateinit var dashboardAdapter: DashboardAdapter
    lateinit var viewPager2: ViewPager2
    lateinit var tabLayout: TabLayout
    lateinit var logout: ImageView

    val titleList = listOf("Vehicle Entry", "Vehicle List", "Reports", "Admin")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        (activity as MainActivity).setSupportActionBar(view.findViewById(R.id.toolbar))

        viewPager2 = view.findViewById(R.id.viewPager2)
        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        dashboardAdapter = DashboardAdapter(childFragmentManager, lifecycle)

        viewPager2.adapter = dashboardAdapter

        tabLayout = view.findViewById(R.id.tabLayout)

        TabLayoutMediator(tabLayout, viewPager2){ tab, position ->
            tab.text = titleList[position]
        }.attach()

        logout = view.findViewById(R.id.logout)
        logout.setOnClickListener {
            AuthenticationManager.logout()
            requireContext().startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        return view

    }

}
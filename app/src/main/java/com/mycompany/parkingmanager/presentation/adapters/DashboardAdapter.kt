package com.mycompany.parkingmanager.presentation.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mycompany.parkingmanager.presentation.ui.fragments.AdminFragment
import com.mycompany.parkingmanager.presentation.ui.fragments.ReportsFragment
import com.mycompany.parkingmanager.presentation.ui.fragments.VehicleEntryFragment
import com.mycompany.parkingmanager.presentation.ui.fragments.VehicleListFragment

class DashboardAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(
    fragmentManager, lifecycle){

    var fragmentList = listOf(
        VehicleEntryFragment(),
        VehicleListFragment(),
        ReportsFragment(),
        AdminFragment()
    )

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    override fun getItemCount(): Int = fragmentList.size


}

package com.trixxwids.app.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.trixxwids.app.R
import com.trixxwids.app.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pages = listOf(
            PageData(
                android.R.drawable.ic_menu_edit,
                "Welcome to TrixxWids",
                "Create beautiful iOS-style widgets for your Android home screen"
            ),
            PageData(
                android.R.drawable.ic_menu_gallery,
                "Build with Canvas",
                "Drag, resize, and style widgets visually with our canvas editor"
            ),
            PageData(
                android.R.drawable.ic_menu_compass,
                "Add to Home Screen",
                "Place your custom widgets directly on your home screen"
            )
        )

        val adapter = OnboardingPagerAdapter(this, pages)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabIndicator, binding.viewPager) { tab, _ ->
            tab.view.isEnabled = false
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == pages.size - 1) {
                    binding.btnNext.text = getString(R.string.onboarding_get_started)
                } else {
                    binding.btnNext.text = getString(R.string.onboarding_next)
                }
            }
        })

        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < pages.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        getSharedPreferences("trixxwids_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_complete", true)
            .apply()
        finish()
    }

    private data class PageData(
        val imageRes: Int,
        val title: String,
        val description: String
    )

    private class OnboardingPagerAdapter(
        activity: OnboardingActivity,
        private val pages: List<PageData>
    ) : androidx.viewpager2.adapter.FragmentStateAdapter(activity) {
        override fun getItemCount() = pages.size

        override fun createFragment(position: Int) = OnboardingPageFragment(pages[position])
    }

    private class OnboardingPageFragment(
        private val pageData: PageData
    ) : androidx.fragment.app.Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return inflater.inflate(R.layout.onboarding_page, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            view.findViewById<ImageView>(R.id.onboarding_image).setImageResource(pageData.imageRes)
            view.findViewById<TextView>(R.id.onboarding_title).text = pageData.title
            view.findViewById<TextView>(R.id.onboarding_description).text = pageData.description
        }
    }
}

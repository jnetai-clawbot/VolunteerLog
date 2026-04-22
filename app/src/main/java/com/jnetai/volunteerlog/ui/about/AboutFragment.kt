package com.jnetai.volunteerlog.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jnetai.volunteerlog.BuildConfig
import com.jnetai.volunteerlog.R
import com.jnetai.volunteerlog.databinding.FragmentAboutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvVersion.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"

        binding.btnCheckUpdates.setOnClickListener {
            binding.tvUpdateStatus.visibility = View.VISIBLE
            binding.tvUpdateStatus.text = getString(R.string.checking_updates)

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        val json = URL("https://api.github.com/repos/jnetai-clawbot/VolunteerLog/releases/latest").readText()
                        val obj = JSONObject(json)
                        obj.optString("tag_name", "")
                    }
                    if (result.isNotEmpty() && result != "v${BuildConfig.VERSION_NAME}") {
                        binding.tvUpdateStatus.text = getString(R.string.update_available, result)
                    } else {
                        binding.tvUpdateStatus.text = getString(R.string.up_to_date)
                    }
                } catch (e: Exception) {
                    binding.tvUpdateStatus.text = getString(R.string.error_checking_updates)
                }
            }
        }

        binding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
        }

        binding.btnGithub.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jnetai-clawbot/VolunteerLog")))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
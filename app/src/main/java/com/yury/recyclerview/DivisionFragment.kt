package com.yury.recyclerview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.yury.recyclerview.databinding.FragmentDivisionBinding

class DivisionFragment : Fragment() {
    private lateinit var binding: FragmentDivisionBinding
    private val scanLauncher = registerForActivityResult(ScanContract()){
        result ->
        if (result.contents == null){

        } else {
            binding.textinInDF.text = result.contents
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDivisionBinding.inflate(layoutInflater, container, false)

        binding.button2.setOnClickListener { scanQR() }

        return binding.root
    }

    private fun scanQR() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
        options.setPrompt("Scan a barcode")
        options.setCameraId(0) // Use a specific camera of the device

        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)

        scanLauncher.launch(options)
    }


}
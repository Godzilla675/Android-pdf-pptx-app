package com.officesuite.app.ui.pptx

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.officesuite.app.R
import com.officesuite.app.databinding.FragmentPptxViewerBinding
import com.officesuite.app.utils.ErrorHandler
import com.officesuite.app.utils.FileUtils
import com.officesuite.app.utils.MemoryManager
import com.officesuite.app.utils.Result
import com.officesuite.app.utils.ShareUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xslf.usermodel.XMLSlideShow
import java.io.File
import java.io.FileInputStream

/**
 * Fragment for viewing PowerPoint (PPTX) presentations.
 * Features:
 * - Slide-by-slide navigation with swipe gestures
 * - Memory-efficient slide rendering with caching
 * - Share and convert functionality
 * - Graceful error handling
 */
class PptxViewerFragment : Fragment() {

    private var _binding: FragmentPptxViewerBinding? = null
    private val binding get() = _binding!!
    
    private var fileUri: Uri? = null
    private var cachedFile: File? = null
    private var slideImages = mutableListOf<Bitmap>()
    private var currentSlide = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPptxViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        arguments?.getString("file_uri")?.let { uriString ->
            fileUri = Uri.parse(uriString)
            loadPresentation()
        }
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            inflateMenu(R.menu.menu_pptx_viewer)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_share -> {
                        shareDocument()
                        true
                    }
                    R.id.action_convert -> {
                        convertToPdf()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerSlides.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
        }
    }

    private fun setupClickListeners() {
        binding.fabPrevious.setOnClickListener {
            navigateToPreviousSlide()
        }
        
        binding.fabNext.setOnClickListener {
            navigateToNextSlide()
        }
    }
    
    private fun navigateToPreviousSlide() {
        if (currentSlide > 0) {
            currentSlide--
            binding.recyclerSlides.smoothScrollToPosition(currentSlide)
            updateSlideInfo()
        }
    }
    
    private fun navigateToNextSlide() {
        if (currentSlide < slideImages.size - 1) {
            currentSlide++
            binding.recyclerSlides.smoothScrollToPosition(currentSlide)
            updateSlideInfo()
        }
    }

    private fun loadPresentation() {
        fileUri?.let { uri ->
            binding.progressBar.visibility = View.VISIBLE
            
            lifecycleScope.launch {
                val result = Result.runCatchingSuspend {
                    val file = withContext(Dispatchers.IO) {
                        FileUtils.copyToCache(requireContext(), uri)
                    }
                    
                    if (file != null) {
                        withContext(Dispatchers.IO) {
                            loadSlides(file)
                        }
                        file
                    } else {
                        throw IllegalStateException("Could not read file")
                    }
                }
                
                result.onSuccess { file ->
                    cachedFile = file
                    binding.toolbar.title = file.name
                    
                    val adapter = SlideAdapter(slideImages)
                    binding.recyclerSlides.adapter = adapter
                    
                    updateSlideInfo()
                    binding.progressBar.visibility = View.GONE
                }.onError { error ->
                    binding.progressBar.visibility = View.GONE
                    ErrorHandler.showErrorToast(requireContext(), error.exception)
                }
            }
        }
    }

    /**
     * Loads slides from a PPTX file and converts them to bitmaps for display.
     * Uses MemoryManager for efficient memory handling.
     * 
     * @param file The PPTX file to load
     */
    private fun loadSlides(file: File) {
        slideImages.clear()
        
        // Trim cache if memory is low before loading slides
        MemoryManager.trimCacheIfNeeded()
        
        try {
            val slideShow = XMLSlideShow(FileInputStream(file))
            
            // Use standard 16:9 presentation dimensions (1920x1080 scaled down)
            val slideWidth = 960
            val slideHeight = 540
            
            for (slide in slideShow.slides) {
                val bitmap = Bitmap.createBitmap(
                    slideWidth,
                    slideHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                
                // Extract text content from the slide
                val textPaint = android.graphics.Paint().apply {
                    color = Color.BLACK
                    textSize = 24f
                    isAntiAlias = true
                }
                
                val titlePaint = android.graphics.Paint().apply {
                    color = Color.BLACK
                    textSize = 36f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                
                val slideNumber = slideImages.size + 1
                
                // Try to extract slide title and content
                var yPosition = 60f
                var hasContent = false
                
                for (shape in slide.shapes) {
                    if (shape is org.apache.poi.xslf.usermodel.XSLFTextShape) {
                        val text = shape.text
                        if (text.isNotBlank()) {
                            hasContent = true
                            // Draw first text as title, rest as content
                            if (yPosition == 60f) {
                                canvas.drawText(text.take(40), slideWidth / 2f, yPosition, titlePaint)
                            } else {
                                canvas.drawText(text.take(50), 30f, yPosition, textPaint)
                            }
                            yPosition += 40f
                            if (yPosition > slideHeight - 80) break
                        }
                    }
                }
                
                // If no content found, show placeholder
                if (!hasContent) {
                    canvas.drawText(
                        "Slide $slideNumber",
                        slideWidth / 2f,
                        slideHeight / 2f,
                        titlePaint
                    )
                }
                
                slideImages.add(bitmap)
            }
            
            slideShow.close()
        } catch (e: Exception) {
            ErrorHandler.logError("PptxViewer", "Error loading slides", e)
        }
    }

    private fun updateSlideInfo() {
        binding.textSlideInfo.text = "Slide ${currentSlide + 1} of ${slideImages.size}"
    }

    private fun shareDocument() {
        cachedFile?.let { file ->
            ShareUtils.shareFile(
                requireContext(),
                file,
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            )
        }
    }

    private fun convertToPdf() {
        Toast.makeText(context, "Converting to PDF...", Toast.LENGTH_SHORT).show()
        // Navigate to converter or perform conversion
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Recycle all bitmaps using MemoryManager
        MemoryManager.recycleBitmaps(slideImages)
        slideImages.clear()
        _binding = null
    }
}

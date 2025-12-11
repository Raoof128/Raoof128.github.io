/*
 * Copyright 2024 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qrshield.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import com.qrshield.model.ContentType

/**
 * Image Processing Utilities for QR Scanner
 * 
 * Provides helper functions for image decoding, resizing, and content type detection.
 * Extracted from AndroidQrScanner for better separation of concerns.
 * 
 * SECURITY NOTES:
 * - All image dimensions are bounded to prevent OOM
 * - Content validation is performed before processing
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
object ImageProcessingUtils {
    
    /** Maximum image dimension for gallery scanning */
    const val MAX_IMAGE_DIMENSION = 2048
    
    /** Maximum image size in bytes (10MB) */
    const val MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024
    
    /** Maximum QR content length */
    const val MAX_CONTENT_LENGTH = 4096
    
    /**
     * Calculate sample size for image decoding.
     * 
     * Ensures images are downscaled to prevent OOM while maintaining quality
     * sufficient for QR code detection.
     * 
     * @param width Original image width
     * @param height Original image height
     * @param maxDimension Maximum allowed dimension (default: MAX_IMAGE_DIMENSION)
     * @return Sample size to use for BitmapFactory options
     */
    fun calculateSampleSize(
        width: Int, 
        height: Int, 
        maxDimension: Int = MAX_IMAGE_DIMENSION
    ): Int {
        var sampleSize = 1
        val currentMax = maxOf(width, height)
        
        while (currentMax / sampleSize > maxDimension) {
            sampleSize *= 2
        }
        
        return sampleSize
    }
    
    /**
     * Decode bitmap from byte array with memory-safe settings.
     * 
     * @param imageBytes Raw image data
     * @return Decoded bitmap or null if failed
     */
    fun decodeBitmapSafely(imageBytes: ByteArray): Bitmap? {
        if (imageBytes.isEmpty() || imageBytes.size > MAX_IMAGE_SIZE_BYTES) {
            return null
        }
        
        // First pass: get dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
        
        // Calculate sample size
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight)
        options.inJustDecodeBounds = false
        
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
    }
    
    /**
     * Decode bitmap from content URI with memory-safe settings.
     * 
     * @param context Application context
     * @param uri Content URI to image
     * @return Decoded bitmap or null if failed
     */
    fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            // First pass: get dimensions
            val optionsForSize = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, optionsForSize)
            }
            
            // Calculate sample size
            val sampleSize = calculateSampleSize(optionsForSize.outWidth, optionsForSize.outHeight)
            
            // Second pass: decode with sample size
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Detect content type from raw string content.
     * 
     * @param content Raw QR code content
     * @return Detected ContentType
     */
    fun detectContentType(content: String): ContentType {
        return when {
            content.startsWith("http://", ignoreCase = true) || 
            content.startsWith("https://", ignoreCase = true) -> ContentType.URL
            
            content.startsWith("WIFI:", ignoreCase = true) -> ContentType.WIFI
            content.startsWith("BEGIN:VCARD", ignoreCase = true) -> ContentType.VCARD
            content.startsWith("geo:", ignoreCase = true) -> ContentType.GEO
            content.startsWith("tel:", ignoreCase = true) -> ContentType.PHONE
            
            content.startsWith("sms:", ignoreCase = true) || 
            content.startsWith("smsto:", ignoreCase = true) -> ContentType.SMS
            
            content.startsWith("mailto:", ignoreCase = true) -> ContentType.EMAIL
            content.startsWith("BEGIN:VEVENT", ignoreCase = true) -> ContentType.TEXT  // Calendar events treated as text
            
            else -> ContentType.TEXT
        }
    }
    
    /**
     * Validate QR content for security.
     * 
     * @param content Raw content string
     * @return true if content is safe to process
     */
    fun isContentSafe(content: String?): Boolean {
        if (content == null) return false
        if (content.isEmpty()) return false
        if (content.length > MAX_CONTENT_LENGTH) return false
        return true
    }
}

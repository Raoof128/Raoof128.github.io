//
// Copyright 2024 QR-SHIELD Contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

// UI/Components/ImagePicker.swift
// QR-SHIELD Image Picker - iOS 17+ Compatible
//
// UPDATED: December 2025
// - Uses native SwiftUI PhotosPicker
// - Vision framework for QR detection
// - Simplified without VisionKit document scanner

import SwiftUI
import PhotosUI
import Vision
#if os(iOS)
import UIKit

// MARK: - Image Picker View

/// Modern SwiftUI PhotosPicker for selecting images with QR codes
struct ImagePicker: View {
    let onImagePicked: (UIImage) -> Void
    
    @State private var selectedItem: PhotosPickerItem?
    @State private var isLoading = false
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                // Header
                VStack(spacing: 8) {
                    Image(systemName: "photo.on.rectangle.angled")
                        .font(.system(size: 60))
                        .foregroundStyle(LinearGradient.brandGradient)
                        .symbolEffect(.pulse)
                    
                    Text("Select an Image")
                        .font(.title2.weight(.semibold))
                        .foregroundColor(.textPrimary)
                    
                    Text("Choose a photo containing a QR code")
                        .font(.subheadline)
                        .foregroundColor(.textSecondary)
                }
                .padding(.top, 40)
                
                Spacer()
                
                // Photo Picker Button
                PhotosPicker(
                    selection: $selectedItem,
                    matching: .images,
                    photoLibrary: .shared()
                ) {
                    HStack(spacing: 12) {
                        if isLoading {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Image(systemName: "photo.fill")
                        }
                        Text(isLoading ? "Loading..." : "Choose from Library")
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 18)
                    .background {
                        RoundedRectangle(cornerRadius: 16)
                            .fill(LinearGradient.brandGradient)
                            .overlay {
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(Color.white.opacity(0.2), lineWidth: 1)
                            }
                    }
                    .shadow(color: .brandPrimary.opacity(0.4), radius: 10, y: 4)
                }
                .padding(.horizontal, 24)
                .onChange(of: selectedItem) { _, newValue in
                    handleSelection(newValue)
                }
                
                Spacer()
            }
            .liquidGlassBackground()
            .navigationTitle("Import QR Code")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                    .foregroundColor(.brandPrimary)
                }
            }
        }
    }
    
    private func handleSelection(_ item: PhotosPickerItem?) {
        guard let item else { return }
        
        isLoading = true
        
        Task {
            if let data = try? await item.loadTransferable(type: Data.self),
               let image = UIImage(data: data) {
                await MainActor.run {
                    isLoading = false
                    onImagePicked(image)
                    dismiss()
                }
            } else {
                await MainActor.run {
                    isLoading = false
                }
            }
        }
    }
}

// MARK: - QR Image Scanner (Vision Framework)

/// Actor that scans images for QR codes using Vision framework
actor QRImageScanner {
    
    /// Scan an image for QR codes
    func scanQRCode(from image: UIImage) async throws -> String? {
        guard let cgImage = image.cgImage else {
            throw ScanError.invalidImage
        }
        
        return try await withCheckedThrowingContinuation { continuation in
            let request = VNDetectBarcodesRequest { request, error in
                if let error {
                    continuation.resume(throwing: error)
                    return
                }
                
                guard let observations = request.results as? [VNBarcodeObservation] else {
                    continuation.resume(returning: nil)
                    return
                }
                
                // Find QR codes
                let qrCode = observations.first { observation in
                    observation.symbology == .qr
                }
                
                continuation.resume(returning: qrCode?.payloadStringValue)
            }
            
            request.symbologies = [.qr]
            
            let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
            
            do {
                try handler.perform([request])
            } catch {
                continuation.resume(throwing: error)
            }
        }
    }
    
    enum ScanError: Error {
        case invalidImage
        case noQRCode
    }
}

#endif


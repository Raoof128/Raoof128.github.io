// UI/Components/ImagePicker.swift
// QR-SHIELD Image Picker - iOS 26 Edition
//
// UPDATED: December 2025 - iOS 26 PhotosPicker API
// Uses native SwiftUI PhotosPicker when available

import SwiftUI
import PhotosUI

// MARK: - iOS 26 Native PhotosPicker Wrapper

/// Modern SwiftUI PhotosPicker for iOS 26
/// Falls back to UIViewControllerRepresentable for older versions
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
                                    .stroke(Color.white.opacity(0.2), lineWidth: 0.5)
                            }
                    }
                    .shadow(color: .brandPrimary.opacity(0.4), radius: 12, y: 6)
                }
                .disabled(isLoading)
                .padding(.horizontal, 24)
                .sensoryFeedback(.impact(weight: .light), trigger: selectedItem)
                
                // Recent Photos hint
                Text("Or drag and drop an image")
                    .font(.caption)
                    .foregroundColor(.textMuted)
                
                Spacer()
            }
            .background {
                MeshGradient.liquidGlassBackground
                    .ignoresSafeArea()
            }
            .navigationTitle("Photo Library")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                    .foregroundColor(.brandPrimary)
                }
            }
            .onChange(of: selectedItem) { _, newValue in
                guard let item = newValue else { return }
                loadImage(from: item)
            }
        }
    }
    
    private func loadImage(from item: PhotosPickerItem) {
        isLoading = true
        
        Task {
            do {
                if let data = try await item.loadTransferable(type: Data.self),
                   let image = UIImage(data: data) {
                    await MainActor.run {
                        onImagePicked(image)
                        dismiss()
                    }
                }
            } catch {
                print("Failed to load image: \(error)")
            }
            
            await MainActor.run {
                isLoading = false
            }
        }
    }
}

// MARK: - Vision-based QR Scanner from Image

/// Scans QR codes from images using Vision framework
actor QRImageScanner {
    func scanQRCode(from image: UIImage) async throws -> String? {
        guard let cgImage = image.cgImage else { return nil }
        
        return try await withCheckedThrowingContinuation { continuation in
            let request = VNDetectBarcodesRequest { request, error in
                if let error {
                    continuation.resume(throwing: error)
                    return
                }
                
                guard let results = request.results as? [VNBarcodeObservation],
                      let firstQR = results.first(where: { $0.symbology == .qr }),
                      let payload = firstQR.payloadStringValue else {
                    continuation.resume(returning: nil)
                    return
                }
                
                continuation.resume(returning: payload)
            }
            
            request.symbologies = [.qr, .aztec, .dataMatrix, .pdf417]
            
            let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
            
            do {
                try handler.perform([request])
            } catch {
                continuation.resume(throwing: error)
            }
        }
    }
}

import Vision

// MARK: - Document Scanner (iOS 26)

/// Allows scanning documents with camera for embedded QR codes
struct DocumentScanner: UIViewControllerRepresentable {
    let onScan: (UIImage) -> Void
    
    @Environment(\.dismiss) private var dismiss
    
    func makeUIViewController(context: Context) -> VNDocumentCameraViewController {
        let scanner = VNDocumentCameraViewController()
        scanner.delegate = context.coordinator
        return scanner
    }
    
    func updateUIViewController(_ uiViewController: VNDocumentCameraViewController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, VNDocumentCameraViewControllerDelegate {
        let parent: DocumentScanner
        
        init(_ parent: DocumentScanner) {
            self.parent = parent
        }
        
        func documentCameraViewController(
            _ controller: VNDocumentCameraViewController,
            didFinishWith scan: VNDocumentCameraScan
        ) {
            parent.dismiss()
            
            // Get first page
            if scan.pageCount > 0 {
                let image = scan.imageOfPage(at: 0)
                parent.onScan(image)
            }
        }
        
        func documentCameraViewControllerDidCancel(_ controller: VNDocumentCameraViewController) {
            parent.dismiss()
        }
        
        func documentCameraViewController(
            _ controller: VNDocumentCameraViewController,
            didFailWithError error: Error
        ) {
            parent.dismiss()
            print("Document scan failed: \(error)")
        }
    }
}

// MARK: - Legacy Camera Picker (Fallback)

struct CameraPicker: UIViewControllerRepresentable {
    let onImageCaptured: (UIImage) -> Void
    
    @Environment(\.dismiss) private var dismiss
    
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.delegate = context.coordinator
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: CameraPicker
        
        init(_ parent: CameraPicker) {
            self.parent = parent
        }
        
        func imagePickerController(
            _ picker: UIImagePickerController,
            didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]
        ) {
            parent.dismiss()
            
            if let image = info[.originalImage] as? UIImage {
                parent.onImageCaptured(image)
            }
        }
        
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.dismiss()
        }
    }
}

// MARK: - Preview

#Preview {
    ImagePicker { image in
        print("Selected image: \(image.size)")
    }
}

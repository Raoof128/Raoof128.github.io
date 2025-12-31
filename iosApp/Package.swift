// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "MehrGuard",
    defaultLocalization: "en",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(
            name: "MehrGuard",
            targets: ["MehrGuard"]),
    ],
    targets: [
        .target(
            name: "MehrGuard",
            dependencies: [],
            path: "MehrGuard",
            exclude: [
                "Info.plist",
                "README_PERMISSIONS.md"
            ],
            resources: [
                .process("en.lproj"),
                .process("Assets.xcassets"),
                .process("PrivacyInfo.xcprivacy")
            ]),
    ]
)

// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "QRShield",
    defaultLocalization: "en",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(
            name: "QRShield",
            targets: ["QRShield"]),
    ],
    targets: [
        .target(
            name: "QRShield",
            dependencies: [],
            path: "QRShield",
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

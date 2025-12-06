// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "QRShield",
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
            path: "QRShield"),
    ]
)

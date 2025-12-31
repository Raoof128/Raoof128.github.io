#!/bin/bash

# Mehr Guard iOS Asset Import Script
# Copies and resizes assets from source folder to Xcode asset catalog
#
# Usage: ./import_assets.sh /path/to/source/assets
# Requirements: ImageMagick (brew install imagemagick)

set -e

SOURCE_DIR="${1:-/Users/raoof.r12/Desktop/Raouf/K/Assets}"
TARGET_DIR="/Users/raoof.r12/Desktop/Raouf/K/mehr-guard/iosApp/MehrGuard/Assets.xcassets"

echo "🎨 Mehr Guard Asset Importer"
echo "=========================="
echo "Source: $SOURCE_DIR"
echo "Target: $TARGET_DIR"
echo ""

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null; then
    echo "⚠️  ImageMagick not found. Installing via Homebrew..."
    brew install imagemagick
fi

# Function to resize and copy image
resize_image() {
    local source="$1"
    local target="$2"
    local size="$3"
    
    if [ -f "$source" ]; then
        convert "$source" -resize "${size}x${size}" -background none "$target"
        echo "  ✓ Created $target (${size}x${size})"
    else
        echo "  ⚠ Source not found: $source"
    fi
}

# Function to create imageset
create_imageset() {
    local name="$1"
    local source="$2"
    local base_size="$3"
    
    echo ""
    echo "📦 Creating $name.imageset..."
    
    local dir="$TARGET_DIR/$name.imageset"
    mkdir -p "$dir"
    
    # Calculate sizes
    local size_1x=$base_size
    local size_2x=$((base_size * 2))
    local size_3x=$((base_size * 3))
    
    resize_image "$source" "$dir/${name,,}.png" "$size_1x"
    resize_image "$source" "$dir/${name,,}@2x.png" "$size_2x"
    resize_image "$source" "$dir/${name,,}@3x.png" "$size_3x"
}

echo ""
echo "📱 Step 1: App Icon (1024x1024)"
echo "-------------------------------"
APP_ICON_DIR="$TARGET_DIR/AppIcon.appiconset"
mkdir -p "$APP_ICON_DIR"

if [ -f "$SOURCE_DIR/ic_launcher.png" ]; then
    resize_image "$SOURCE_DIR/ic_launcher.png" "$APP_ICON_DIR/app-icon-1024.png" 1024
    # Create dark variant (same as regular for now)
    cp "$APP_ICON_DIR/app-icon-1024.png" "$APP_ICON_DIR/app-icon-1024-dark.png"
    # Create tinted variant (grayscale)
    convert "$SOURCE_DIR/ic_launcher.png" -resize 1024x1024 -colorspace Gray "$APP_ICON_DIR/app-icon-1024-tinted.png"
    echo "  ✓ Created tinted variant (grayscale)"
fi

echo ""
echo "🛡️ Step 2: Verdict Icons"
echo "-------------------------"

# Shield Safe (from ic_success.png)
if [ -f "$SOURCE_DIR/ic_success.png" ]; then
    create_imageset "ShieldSafe" "$SOURCE_DIR/ic_success.png" 40
fi

# Shield Warning (from ic_warning.png)
if [ -f "$SOURCE_DIR/ic_warning.png" ]; then
    create_imageset "ShieldWarning" "$SOURCE_DIR/ic_warning.png" 40
fi

# Shield Danger (from danger_mode.png)
if [ -f "$SOURCE_DIR/danger_mode.png" ]; then
    create_imageset "ShieldDanger" "$SOURCE_DIR/danger_mode.png" 40
fi

echo ""
echo "📁 Step 3: Navigation Icons"
echo "---------------------------"

# History icon
if [ -f "$SOURCE_DIR/ic_history.png" ]; then
    create_imageset "IconHistory" "$SOURCE_DIR/ic_history.png" 28
fi

# Settings icon
if [ -f "$SOURCE_DIR/ic_setting.png" ]; then
    create_imageset "IconSettings" "$SOURCE_DIR/ic_setting.png" 28
fi

# Gallery icon
if [ -f "$SOURCE_DIR/ic_import.png" ]; then
    create_imageset "IconGallery" "$SOURCE_DIR/ic_import.png" 28
fi

echo ""
echo "🚀 Step 4: Launch & Branding"
echo "----------------------------"

# Launch Logo
if [ -f "$SOURCE_DIR/splash_screen.png" ]; then
    create_imageset "LaunchLogo" "$SOURCE_DIR/splash_screen.png" 200
fi

# Branding Logo
if [ -f "$SOURCE_DIR/ic_launcher.png" ]; then
    create_imageset "BrandingLogo" "$SOURCE_DIR/ic_launcher.png" 100
fi

# Danger Alert
if [ -f "$SOURCE_DIR/RED ALERT ANIMATION FRAMES.png" ]; then
    create_imageset "DangerAlert" "$SOURCE_DIR/RED ALERT ANIMATION FRAMES.png" 200
fi

echo ""
echo "✅ Asset import complete!"
echo ""
echo "📝 Next Steps:"
echo "  1. Open Xcode project"
echo "  2. Verify assets in Asset Catalog"
echo "  3. Build and test on simulator"
echo ""

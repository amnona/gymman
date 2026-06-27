#!/usr/bin/env python3
"""Generate dumbbell app icons for Android"""

from PIL import Image, ImageDraw
import os

# Define icon sizes and their corresponding folders
sizes = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192,
}

# Colors
BACKGROUND_COLOR = (30, 58, 138)  # Dark blue #1E3A8A
WEIGHT_COLOR_OUTER = (255, 107, 53)  # #FF6B35
WEIGHT_COLOR_INNER = (255, 140, 66)  # #FF8C42
BAR_COLOR = (44, 62, 80)  # #2C3E50
GRIP_COLOR = (52, 73, 94)  # #34495E

def create_dumbbell_icon(size):
    """Create a dumbbell icon of the specified size"""
    # Create image with background
    img = Image.new('RGBA', (size, size), BACKGROUND_COLOR + (255,))
    draw = ImageDraw.Draw(img)

    # Scale coordinates based on size
    scale = size / 108

    # Left weight plate (outer circle)
    left_plate_x = int(24 * scale)
    left_plate_y = int(54 * scale)
    left_plate_r = int(12 * scale)
    draw.ellipse(
        [(left_plate_x - left_plate_r, left_plate_y - left_plate_r),
         (left_plate_x + left_plate_r, left_plate_y + left_plate_r)],
        fill=WEIGHT_COLOR_OUTER
    )

    # Left weight plate (inner circle)
    inner_r = int(10 * scale)
    draw.ellipse(
        [(left_plate_x - inner_r, left_plate_y - inner_r),
         (left_plate_x + inner_r, left_plate_y + inner_r)],
        fill=WEIGHT_COLOR_INNER
    )

    # Bar
    bar_left = int(36 * scale)
    bar_top = int(51 * scale)
    bar_right = int(72 * scale)
    bar_bottom = int(57 * scale)
    draw.rectangle([(bar_left, bar_top), (bar_right, bar_bottom)], fill=BAR_COLOR)

    # Left grip section
    grip_left = int(34 * scale)
    grip_top = int(48 * scale)
    grip_right = int(38 * scale)
    grip_bottom = int(60 * scale)
    draw.rectangle([(grip_left, grip_top), (grip_right, grip_bottom)], fill=GRIP_COLOR)

    # Right grip section
    grip_left2 = int(70 * scale)
    grip_right2 = int(74 * scale)
    draw.rectangle([(grip_left2, grip_top), (grip_right2, grip_bottom)], fill=GRIP_COLOR)

    # Right weight plate (outer circle)
    right_plate_x = int(84 * scale)
    right_plate_y = int(54 * scale)
    draw.ellipse(
        [(right_plate_x - left_plate_r, right_plate_y - left_plate_r),
         (right_plate_x + left_plate_r, right_plate_y + left_plate_r)],
        fill=WEIGHT_COLOR_OUTER
    )

    # Right weight plate (inner circle)
    draw.ellipse(
        [(right_plate_x - inner_r, right_plate_y - inner_r),
         (right_plate_x + inner_r, right_plate_y + inner_r)],
        fill=WEIGHT_COLOR_INNER
    )

    return img

# Create icons for each density
base_path = '/Users/amnon/AndroidStudioProjects/GymTracker/app/src/main/res'

for density, size in sizes.items():
    # Create the image
    img = create_dumbbell_icon(size)

    # Save as webp
    output_dir = os.path.join(base_path, f'mipmap-{density}')
    os.makedirs(output_dir, exist_ok=True)

    # Save ic_launcher.webp
    launcher_path = os.path.join(output_dir, 'ic_launcher.webp')
    img.save(launcher_path, 'WEBP', quality=95)
    print(f"Created {launcher_path}")

    # Save ic_launcher_round.webp (same as regular for now)
    round_path = os.path.join(output_dir, 'ic_launcher_round.webp')
    img.save(round_path, 'WEBP', quality=95)
    print(f"Created {round_path}")

print("\nDumbbell icons generated successfully!")


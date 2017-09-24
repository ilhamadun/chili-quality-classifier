"""
Feature extration.

Extract features from segmented image
"""

import os

import fire
import numpy as np

import cv2
import segmentation
import utilities


def feature_extration(src, save_to):
    """Extract feature from segmented images.

    Args:
    - `src`: source directory containing segmented images
    - `save_to`: path to save the features

    """
    features = extract_features_from_directory(src)

    header = "Blue,Green,Red,Area,Perimeter"
    np.savetxt(save_to, features, '%.5f', ',', header=header, comments='')

    print('Features saved to', save_to)


def extract_features_from_directory(src):
    """Extract feature of images from a directory.

    Args:
    - `src`: source directory containing segmented images

    Returns:
    A numpy array of features from all images in `src` directory

    """
    features = []
    for root, _, files in os.walk(src):
        files.sort(key=utilities.natural_keys)

        for filename in files:
            path = os.path.join(root, filename)
            print('Extracting image from', path)

            image = cv2.imread(path)
            features.append(extract_image_features(image))

    return np.array(features)


def extract_image_features(image):
    """Extract feature from a single image.

    The extracted features are:
    - Mean color
    - Total contour area
    - Total contour perimeter

    Args:
    - `image`: OpenCV image object

    Returns:
    A numpy array of features containing three mean color, area and perimeter

    """
    segmented, mask, contours = segmentation.find_segment(image)

    mean_color = extract_mean_color(segmented, mask)
    area = extract_area(contours)
    perimeter = extract_perimeter(contours)

    features = np.concatenate([mean_color, [area, perimeter]])

    return features


def extract_mean_color(image, mask):
    """Count mean color of the image covered in mask."""
    return cv2.mean(image, mask=mask)[:3]


def extract_area(contours):
    """Find total area of the contour."""
    area = 0
    for contour in contours:
        area += cv2.contourArea(contour)

    return area


def extract_perimeter(contours):
    """Find total perimeter of the contour."""
    perimeter = 0
    for contour in contours:
        perimeter += cv2.arcLength(contour, True)

    return perimeter


if __name__ == '__main__':
    fire.Fire(feature_extration)

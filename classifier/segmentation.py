"""
Image segmentation.

Separate chili image from the background.
"""

import os

import fire
import numpy as np
from scipy.signal import savgol_filter

import cv2
import utilities


def segmentation(src, save_dir):
    """Extract images from a directory and save the result.

    Args:
    - `src`: source directory
    - `save_dir`: directory to save the segmented image

    """
    for root, _, files in os.walk(src):
        files.sort(key=utilities.natural_keys)

        for filename in files:
            path = os.path.join(root, filename)
            print('Segmentation for image from ', path)
            image = cv2.imread(path)
            segmented, _, _ = find_segment(image)

            cv2.imwrite(os.path.join(save_dir, filename), segmented)


def find_segment(image):
    """Subtract chili image from the background."""
    image, _, _ = segmentation_by_edge(image)
    image, mask, contours = segmentation_by_color(image)

    return image, mask, contours


def segmentation_by_edge(image):
    """Apply edge detection to find the object's contour."""
    edges = edge_detection(image)
    edges = np.asarray(edges, np.uint8)
    contours = find_significant_contour(edges, 5 / 100)

    return reduce_image_by_contour(image, edges, contours), edges, contours


def segmentation_by_color(image):
    """Apply color filtering to find the object's contour."""
    image = cv2.medianBlur(image, 3)
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)

    # Green threshold
    lower_range = np.array([30, 20, 0])
    upper_range = np.array([70, 255, 255])
    green_mask = cv2.inRange(hsv, lower_range, upper_range)

    # Lower red threshold
    lower_range = np.array([0, 80, 20])
    upper_range = np.array([30, 255, 230])
    lower_red_mask = cv2.inRange(hsv, lower_range, upper_range)

    # Upper red threshold
    lower_range = np.array([160, 20, 5])
    upper_range = np.array([180, 255, 255])
    upper_red_mask = cv2.inRange(hsv, lower_range, upper_range)

    mask = green_mask + lower_red_mask + upper_red_mask
    contours = find_significant_contour(mask, 3 / 100)
    contours = smoothing(image, contours)

    return reduce_image_by_contour(image, mask, contours), mask, contours


def reduce_image_by_contour(image, mask, contours):
    """Remove background by the contour."""
    mask = fill_poly(mask, contours)

    return reduce_image_by_mask(image, mask)


def fill_poly(mask, contours):
    """Create mask from controur."""
    mask[mask > 0] = 0
    cv2.fillPoly(mask, contours, 255)

    return mask


def reduce_image_by_mask(image, mask):
    """Remove image by the mask."""
    mask = np.logical_not(mask)

    image[mask] = 0

    return image


def edge_detection(image):
    """Detect edges from the image."""
    blurred = cv2.GaussianBlur(image, (3, 3), 0)
    edges = np.max(
        np.array([
            sobel(blurred[:, :, 0]),
            sobel(blurred[:, :, 1]),
            sobel(blurred[:, :, 2])
        ]),
        axis=0)

    mean = np.mean(edges)
    edges[edges <= mean] = 0

    return edges


def sobel(channel):
    """Apply sobel operator to an image channel.

    Args:
    - `channel`: one of the R, G, or B channel from the image

    Returns:
    Detected edges by the Sobel operator

    """
    edge_x = cv2.Sobel(channel, cv2.CV_16S, 1, 0)
    edge_y = cv2.Sobel(channel, cv2.CV_16S, 0, 1)
    edges = np.hypot(edge_x, edge_y)

    edges[edges > 255] = 255

    return edges


def find_significant_contour(mask, size_threshold):
    """Find large contour to detect as object."""
    _, contours, hierarchy = cv2.findContours(mask, cv2.RETR_TREE,
                                              cv2.CHAIN_APPROX_SIMPLE)

    top_level = find_top_level_contour(hierarchy)
    significant = filter_small_contour(mask, contours, top_level,
                                       size_threshold)

    return [x[0] for x in significant]


def find_top_level_contour(hierarchy):
    """Filter top level contours, the ones without parent."""
    top_level = []
    for i, element in enumerate(hierarchy[0]):
        # Each array is in format (Next, Prev, First child, Parent)
        if element[3] == -1:
            element = np.insert(element, 0, [i])
            top_level.append(element)

    return top_level


def filter_small_contour(mask, contours, top_level, threshold):
    """Remove contour small contours."""
    significant = []
    too_small = mask.size * threshold

    for element in top_level:
        contour = contours[element[0]]
        area = cv2.contourArea(contour)

        if area > too_small:
            significant.append([contour, area])

    significant.sort(key=lambda x: x[1])

    return significant


def smoothing(image, contours):
    """Apply savgol filter to smooth the countour boundary."""
    smoothed = []
    for contour in contours:
        window_size = int(round(min(image.shape[0], image.shape[1]) * 0.05))
        x_filter = savgol_filter(contour[:, 0, 0], window_size * 2 + 1, 3)
        y_filter = savgol_filter(contour[:, 0, 1], window_size * 2 + 1, 3)

        approx = np.empty((x_filter.size, 1, 2))
        approx[:, 0, 0] = x_filter
        approx[:, 0, 1] = y_filter
        approx = approx.astype(int)

        smoothed.append(approx)

    return smoothed


if __name__ == '__main__':
    fire.Fire(segmentation)

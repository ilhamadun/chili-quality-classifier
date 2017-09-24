import fire
from feature_extration import feature_extration
from segmentation import segmentation

if __name__ == '__main__':
    fire.Fire({
        'feature_extration': feature_extration,
        'segmentation': segmentation
    })

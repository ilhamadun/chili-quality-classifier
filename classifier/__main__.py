import fire
from classifier import classify
from feature_extration import feature_extration
from regression import regression
from segmentation import segmentation

if __name__ == '__main__':
    fire.Fire({
        'classify': classify,
        'feature_extration': feature_extration,
        'regression': regression,
        'segmentation': segmentation
    })

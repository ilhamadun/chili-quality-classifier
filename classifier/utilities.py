import re


def _atoi(text):
    return int(text) if text.isdigit() else text


def natural_keys(text):
    return [_atoi(c) for c in re.split(r'(\d+)', text)]

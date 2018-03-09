import fire
import matplotlib.pyplot as plt
import numpy as np
from sklearn import svm
from sklearn.model_selection import train_test_split
from sklearn.linear_model import Lasso


def regression(dataset):
    dataset = np.loadtxt(dataset, delimiter=',')

    features = dataset[:, 0:1]
    target = dataset[:, 1:2]

    x_train, x_test, y_train, y_test = train_test_split(
        features, target, test_size=0.3, random_state=0)

    classifier = Lasso()
    classifier.fit(x_train, y_train)
    print(classifier.score(x_test, y_test))
    prediction = classifier.predict(x_test)

    plt.scatter(x_train, y_train, label='data')
    plt.plot(x_test, prediction, label='prediction')
    plt.legend()
    plt.show()


if __name__ == '__main__':
    fire.Fire(regression)

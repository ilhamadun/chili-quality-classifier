import fire
import numpy as np
from scipy.stats import randint, uniform
from sklearn import metrics, svm
from sklearn.model_selection import train_test_split, RandomizedSearchCV
from sklearn.neighbors import KNeighborsClassifier
from sklearn.neural_network import MLPClassifier


def classify(feature_file, label_file, estimator='knn'):
    features = np.loadtxt(feature_file, delimiter=',')
    labels = np.loadtxt(label_file, delimiter=',')

    features = np.reshape(features, (features.shape[0], 1))
    # labels = np.reshape(labels, (labels.shape[0], 1))


    x_train, x_test, y_train, y_test = train_test_split(
        features, labels, test_size=0.3, random_state=0)

    if estimator == 'knn':
        param_distributions = {
            'n_neighbors': randint(1, 21),
            'weights': ['uniform', 'distance'],
            'algorithm': ['ball_tree', 'kd_tree'],
            'leaf_size': randint(20, 51),
            'p': randint(2, 5)
        }

        classifier = KNeighborsClassifier()
    elif estimator == 'svm':
        param_distributions = {
            'kernel': ['linear', 'rbf', 'poly'],
            'degree': randint(2, 5)
        }
        classifier = svm.NuSVC()
    elif estimator == 'mlp':
        param_distributions = {
            'hidden_layer_sizes': randint(8, 32),
            'solver': ['lbfgs'],
            'alpha': uniform(0.00001, 0.002),
        }

        classifier = MLPClassifier(learning_rate="adaptive", max_iter=20000)

    random_search = RandomizedSearchCV(
        classifier,
        param_distributions,
        n_iter=2,
        scoring='accuracy',
        n_jobs=4,
        cv=3,
        refit='accuracy',
        verbose=1)
    random_search.fit(x_train, y_train)

    print("Best CV Score:", random_search.best_score_)
    print("Best CV Parameter:", random_search.best_params_)
    classifier = random_search.best_estimator_
    prediction = classifier.predict(x_test)
    print('Train Accuracy:', classifier.score(x_train, y_train), sep='\t')
    print('Test Accuracy:', classifier.score(x_test, y_test), sep='\t')
    print(
        'Precision:',
        metrics.precision_score(y_test, prediction, average='macro'),
        sep='\t')
    print(
        'Recall:',
        metrics.recall_score(y_test, prediction, average='macro'),
        sep='\t\t')
    print(
        'F1:',
        metrics.f1_score(y_test, prediction, average='macro'),
        sep='\t\t')


if __name__ == '__main__':
    fire.Fire(classify)

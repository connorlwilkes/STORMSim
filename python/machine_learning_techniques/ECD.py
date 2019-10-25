import keras.backend as K
import numpy as np


def main():
    arr_1 = K.variable(np.array([[[200, 200], [100, 100]], [[200, 200], [100, 100]]]))
    arr_2 = K.variable(np.array([[[100, 100], [200, 200]], [[100, 100], [200, 200]]]))
    minus = arr_1 - arr_2
    sqminus = minus ** 2
    print(K.eval(sqminus))
    summed = K.sum(sqminus, axis=-1)
    print(K.eval(summed))
    sqrt = K.sqrt(summed)
    print(K.eval(sqrt))
    print(K.eval(K.mean(K.sqrt(sqrt ** 2))))


if __name__ == "__main__":
    main()

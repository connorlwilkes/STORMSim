import csv
import numpy as np
import os
from PIL import Image
from keras import backend as K
from keras import optimizers
from keras.callbacks import ModelCheckpoint, EarlyStopping
from keras.layers import Input, MaxPooling2D, Convolution2D, Activation, Dense, Reshape, Flatten, BatchNormalization
from keras.models import Model
from keras.utils.vis_utils import plot_model
from scipy.spatial.distance import euclidean


def main():
    x_data, y_loc, y_weights = get_data()
    x_test, x_train = split_data(x_data)
    y_loc_test, y_loc_train = split_data(y_loc)
    y_weights_test, y_weights_train = split_data(y_weights)
    x_train = x_train.astype('float32') / 255.
    x_test = x_test.astype('float32') / 255.
    x_train = np.reshape(x_train, (len(x_train), 208, 208, 1))
    x_test = np.reshape(x_test, (len(x_test), 208, 208, 1))
    nn = VGGCNN((208, 208, 1), './vggcnn.hdf5')
    nn.compile_model(optimiser=optimizers.Adam(lr=0.01),
                     loss={'locations_out': euclidean_rsme_loss, 'number_out': 'mean_squared_error'})
    nn.fit_model(x_train,
                 {'locations_out': y_loc_train, 'number_out': y_weights_train},
                 50, 32,
                 x_test,
                 {'locations_out': y_loc_test, 'number_out': y_weights_test}, )
    results = nn.predict_model(x_train)
    for i in range(1):
        for j in range(len(results[i])):
            pred = results[i]
            actual = y_loc_train[i]
            print("Predicted: {}, Actual: {} Distance: {}".format(pred[j], actual[j], euclidean(pred[j], actual[j])))


def get_data(filepath='./data'):
    x_data = list()
    y_data_loc = list()
    y_data_weights = list()
    for root, dirs, files in os.walk(filepath):
        for file in files:
            file = os.path.join(root, file)
            if file.endswith(".tif"):
                im = Image.open(file)
                im = im.resize((208, 208))
                im_array = np.array(im)
                x_data.append(im_array)
            else:
                reader = csv.reader(open(file))
                row_number = 0
                location = np.zeros((1000, 2))
                weights = np.zeros(1)
                for row in reader:
                    if not row_number == 0:
                        location[row_number - 1][0] = float(row[1])
                        location[row_number - 1][1] = float(row[2])
                    row_number += 1
                y_data_loc.append(location)
                weights[0] = len(location)
                y_data_weights.append(weights)
    return x_data, y_data_loc, y_data_weights


def split_data(data, test_percentage=0.25):
    size = len(data)
    test_number = int(size * test_percentage)
    return np.split(data, [test_number])


class VGGCNN:
    def __init__(self, input_dims, save_path):
        self.input_image = Input(shape=input_dims)
        self._create_model()
        self.checkpoint = ModelCheckpoint(filepath=save_path, verbose=1, save_best_only=True)
        self.early_stopping = EarlyStopping(monitor='val_loss', min_delta=0, patience=2, verbose=0, mode='auto')

    def _create_model(self):
        # x = conv_layer(64, (3, 3))(self.input_image)
        # x = conv_layer(64, (3, 3))(x)
        # x = MaxPooling2D((2, 2), padding='same')(x)
        # x = conv_layer(128, (3, 3))(x)
        # x = conv_layer(128, (3, 3))(x)
        # x = MaxPooling2D((2, 2), padding='same')(x)
        # x = conv_layer(256, (3, 3))(x)
        # x = conv_layer(256, (3, 3))(x)
        # x = conv_layer(256, (3, 3))(x)
        # x = MaxPooling2D((2, 2), padding='same')(x)
        # x = conv_layer(512, (3, 3))(x)
        # x = conv_layer(512, (3, 3))(x)
        # x = conv_layer(512, (3, 3))(x)
        # x = MaxPooling2D((2, 2), padding='same')(x)
        # x = conv_layer(512, (3, 3))(x)
        # x = conv_layer(512, (3, 3))(x)
        # x = conv_layer(512, (3, 3))(x)
        # self.encoded = MaxPooling2D((2, 2), padding='same')(x)
        x = conv_layer(32, (3, 3))(self.input_image)
        x = MaxPooling2D((2, 2), padding='same')(x)
        x = conv_layer(64, (3, 3))(x)
        x = MaxPooling2D((2, 2), padding='same')(x)
        x = conv_layer(128, (3, 3))(x)
        x = MaxPooling2D((2, 2), padding='same')(x)
        x = conv_layer(256, (3, 3))(x)
        x = MaxPooling2D((2, 2), padding='same')(x)
        x = conv_layer(512, (3, 3))(x)
        self.encoded = MaxPooling2D((2, 2), padding='same')(x)
        x = Flatten()(self.encoded)
        # x = Dense(units=4096, activation='relu')(x)
        self.out_number = Dense(units=1, activation='relu', name="number_out")(x)
        x = Dense(units=2000, activation='relu')(x)
        self.out_locations = Reshape((1000, 2), name="locations_out")(x)
        self.model = Model(self.input_image, outputs=[self.out_number, self.out_locations])
        plot_model(self.model, to_file="./cov_net.png", show_shapes=True, show_layer_names=True)
        # self.model = Model(self.input_image, outputs=[self.out_locations])

    def compile_model(self, optimiser, loss):
        self.model.compile(optimizer=optimiser, loss=loss)

    def fit_model(self, x_train, y_train, number_of_epochs, batch_size, x_test, y_test):
        self.model.fit(x_train, y_train,
                       epochs=number_of_epochs,
                       shuffle=True,
                       batch_size=batch_size,
                       verbose=1,
                       validation_data=(x_test, y_test),
                       callbacks=[self.checkpoint, self.early_stopping])

    def predict_model(self, x_train):
        results = self.model.predict(x_train)
        return results


def conv_layer(filter_number, kernel_size):
    def f(input):
        c = Convolution2D(filter_number, kernel_size=kernel_size, strides=(1, 1), padding="same", use_bias=False,
                          kernel_initializer="Orthogonal")(input)
        c = BatchNormalization()(c)
        c = Activation(activation="relu")(c)
        return c

    return f


'Below methods need to return tensors'


def euclidean_rsme_loss(y_true, y_pred):
    minus = y_true - y_pred
    sq_minus = minus ** 2
    summed = K.sum(sq_minus, axis=-1)
    sqrt = K.sqrt(summed)
    return K.mean(K.sqrt(sqrt ** 2))


def jaccard_location_loss(y_true, y_pred):
    # y_true = K.eval(y_true)
    # y_pred = K.eval(y_pred)
    # y_pred, y_true = distance.get_optimal_mapping(y_pred, y_true)
    # return distance.jaccard_index(y_pred, y_true, optimised=True)
    to_return = K.mean(K.square(y_pred - y_true), axis=-1)
    return to_return


def jaccard_weight_loss(y_true, y_pred, smooth=100):
    '''https://github.com/keras-team/keras-contrib/blob/master/keras_contrib/losses/jaccard.py'''
    intersection = K.sum(K.abs(y_true * y_pred), axis=-1)
    sum_ = K.sum(K.abs(y_true) + K.abs(y_pred), axis=-1)
    jac = (intersection + smooth) / (sum_ - intersection + smooth)
    return (1 - jac) * smooth


def gromov_wasserstein_distance_loss(y_true, y_pred):
    y_true = K.eval(y_true)
    y_pred = K.eval(y_pred)
    return distance.gromov_wasserstein_distance(y_pred, y_true)


if __name__ == "__main__":
    main()

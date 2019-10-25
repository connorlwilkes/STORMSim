import os

import matplotlib.pyplot as plt
import numpy as np
from PIL import Image, ImageSequence
from keras import optimizers
from keras.callbacks import ModelCheckpoint, EarlyStopping
from keras.layers import Input, MaxPooling2D, UpSampling2D, BatchNormalization, Convolution2D, Activation, \
    Conv2DTranspose
from keras.models import Model

size = (400, 400, 1)


def main():
    auto_deencoder = AutoDeencoder(size, './auto-deencoder.hdf5')
    data = np.load('/saved_full.npy')
    x_test, x_train = split_data(data, 0.25)
    x_train = x_train.astype('float32') / 255.
    x_test = x_test.astype('float32') / 255.
    x_train = np.reshape(x_train, (len(x_train), size[0], size[1], size[2]))
    x_test = np.reshape(x_test, (len(x_test), size[0], size[1], size[2]))
    auto_deencoder.compile_model(optimiser=optimizers.Adadelta(lr=0.001), loss='binary_crossentropy')
    # auto_deencoder.fit_model(x_train, number_of_epochs=100, x_test=x_test, steps_per_epoch=400)
    auto_deencoder.model.load_weights('/auto-deencoder.hdf5')
    predicted_images = auto_deencoder.predict_model(x_train)
    for i in range(10):
        plt.imshow(predicted_images[i].reshape(size[0], size[1]))
        plt.gray()
        plt.show()


def load_data(path):
    to_return = list()
    for dir, subdir, files in os.walk(path):
        for file in files:
            if file.endswith(".tif"):
                file = os.path.join(dir, file)
                im = Image.open(file)
                for i, frame in enumerate(ImageSequence.Iterator(im)):
                    frame = frame.resize((size[0], size[1]))
                    im_array = np.array(frame)
                    to_return.append(im_array)
                if len(to_return) % 100 == 0:
                    print(len(to_return))
    return np.asarray(to_return)


def split_data(data, test_percentage=0.25):
    size = len(data)
    test_number = int(size * test_percentage)
    return np.split(data, [test_number])


class AutoDeencoder:

    def __init__(self, input_image_dimensions, weights_path):
        self.input_image = Input(shape=input_image_dimensions)
        self.model = Model(self.input_image, self._create_model())
        self.checkpoint = ModelCheckpoint(filepath=weights_path, verbose=1, save_best_only=True)
        self.early_stopping = EarlyStopping(monitor='val_loss', min_delta=0, patience=2, verbose=0, mode='auto')

    def _create_model(self):
        x = conv_layer(64, (3, 3))(self.input_image)
        x = MaxPooling2D((2, 2), padding='same')(x)
        x = conv_layer(32, (3, 3))(x)
        x = MaxPooling2D((2, 2), padding='same')(x)
        x = conv_layer(16, (3, 3))(x)
        x = MaxPooling2D((2, 2), padding='same')(x)
        x = conv_layer(8, (3, 3))(x)
        encoded = MaxPooling2D((2, 2))(x)

        x = conv_layer(8, (3, 3))(encoded)
        x = UpSampling2D((2, 2))(x)
        x = conv_layer(16, (3, 3))(x)
        x = UpSampling2D((2, 2))(x)
        x = conv_layer(32, (3, 3))(x)
        x = UpSampling2D((2, 2))(x)
        x = conv_layer(64, (3, 3))(x)
        x = UpSampling2D((2, 2))(x)
        decoded = Convolution2D(1, kernel_size=(1, 1), strides=(1, 1), padding="same",
                                activation="sigmoid", use_bias=False,
                                kernel_initializer="Orthogonal", name='Prediction')(x)
        print(decoded.shape)
        return decoded

    def compile_model(self, optimiser, loss):
        self.model.compile(optimizer=optimiser, loss=loss)

    def fit_model(self, x_train, number_of_epochs, x_test, steps_per_epoch):
        self.model.fit(x_train, x_train,
                       epochs=number_of_epochs,
                       shuffle=True,
                       batch_size=16,
                       verbose=1,
                       validation_data=(x_test, x_test),
                       callbacks=[self.checkpoint, self.early_stopping])

    def predict_model(self, x_train):
        decoded_imgs = self.model.predict(x_train)
        return decoded_imgs


def conv_layer(filter_number, kernel_size):
    def f(input):
        c = Convolution2D(filter_number, kernel_size=kernel_size, strides=(1, 1), padding="same", use_bias=False,
                          kernel_initializer="Orthogonal")(input)
        c = BatchNormalization()(c)
        c = Activation(activation="relu")(c)
        return c

    return f


def deconv_layer(filter_number, kernel_size):
    def f(input):
        c = Conv2DTranspose(filter_number, kernel_size=kernel_size, strides=(1, 1), padding="same", use_bias=False,
                            kernel_initializer="Orthogonal")(input)
        c = BatchNormalization()(c)
        c = Activation(activation="relu")(c)
        return c

    return f


if __name__ == "__main__":
    main()

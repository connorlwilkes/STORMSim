import csv
from math import *

import matplotlib.pylab as pl
import numpy as np
import ot
import scipy
import scipy.integrate as integrate
from scipy import optimize
from scipy.spatial.distance import euclidean

thread_completion_counter = 0


def main():
    # thread_pool = Pool(10)
    # set_arr = []
    # for i in range(100, 301, 10):
    #     set_arr.append(read_files(i))
    # i = 100
    # for files in set_arr:
    #     plot_point_scatter_data(files[0], files[1], "Scatter plot of Ground Truth against Reconstruction").savefig(
    #         r'C:\Users\virtualreality\IdeaProjects\STORMSim\python\results\psf_variance\plots\scatters\scatterPointPlot{}.png'.format(
    #             i), bbox_inches='tight')
    #     i += 10
    # results = thread_pool.map(calculate_evaluation_methods, set_arr)
    # create_csv(results)
    data = 'example.csv'
    file = open(data)
    figs = plot_evaluation_data(read_results(file))
    figs[0].savefig('/evaluation_methods.png', bbox_inches='tight')
    figs[1].savefig('/evaluation_methodsRSME.png', bbox_inches='tight')
    figs[2].savefig('./evaluation_methods_optimal.png', bbox_inches='tight')


def calculate_evaluation_methods(files):
    result = list()
    opt = get_optimal_mapping(files[0], files[1])
    opt_set_1 = _coordinate_arr_to_set(opt[0])
    opt_set_2 = _coordinate_arr_to_set(opt[1])
    result.append(jaccard_index(files[1], files[0], 100))
    result.append(hausdorff_distance(files[0], files[1]))
    # result.append(gromov_wasserstein_distance(files[0], files[0]))
    result.append(cartesian_rsme(opt[0], opt[1]))
    result.append(jaccard_index(opt[0], opt[1], 100, True))
    result.append(hausdorff_distance(opt_set_1, opt_set_2))
    return result


def read_csv(file):
    reader = csv.reader(file)
    row_number = 0
    to_return = set()
    for row in reader:
        if not row_number == 0:
            coordinate = (round(float(row[1]), 2), round(float(row[2]), 2))
            to_return.add(coordinate)
        row_number += 1
    return to_return


def read_results(file):
    reader = csv.reader(file)
    row_number = 0
    to_return = list()
    for row in reader:
        if not row_number == 0:
            row_list = list()
            row_list.append(row[0])
            row_list.append(row[1])
            row_list.append(row[2])
            row_list.append(row[3])
            row_list.append(row[4])
            row_list.append(row[5])
            to_return.append(row_list)
        row_number += 1
    return to_return


def write_to_csv_file(file, value, end=False):
    if end:
        file.write("\"{}\"\n".format(value))
    else:
        file.write("\"{}\",".format(value))


def create_csv(results):
    file = open('example.csv', 'w+')
    write_to_csv_file(file, "Number of molecules")
    write_to_csv_file(file, "Jaccard Index")
    write_to_csv_file(file, "Hausdorff Distance")
    # write_to_csv_file(file, "Gromov-Wasserstein Distance")
    write_to_csv_file(file, "RSME")
    write_to_csv_file(file, "Jaccard Index (Optimal mapping)")
    write_to_csv_file(file, "Hausdorff Distance (Optimal mapping)", True)
    i = 100
    for result in results:
        write_to_csv_file(file, i)
        write_to_csv_file(file, result[0])
        write_to_csv_file(file, result[1])
        write_to_csv_file(file, result[2])
        write_to_csv_file(file, result[3])
        write_to_csv_file(file, result[4], True)
        # write_to_csv_file(file, result[5], True)
        i += 100
    file.close()


def _coordinate_arr_to_set(arr):
    coordinate_set = set()
    for i in range(len(arr)):
        coordinate = (arr[i][0], arr[i][1])
        coordinate_set.add(coordinate)
    return coordinate_set


def plot_point_scatter_data(set_1, set_2, title):
    arr_1 = _generate_arr(set_1)
    arr_2 = _generate_arr(set_2)
    fig, ax = pl.subplots()
    ax.scatter(arr_1[:, 0], arr_1[:, 1], s=10, c='b', marker="s", label='ThunderSTORM reconstruction')
    ax.scatter(arr_2[:, 0], arr_2[:, 1], s=10, c='r', marker="o", label='Ground Truth')
    ax.set_xlabel("X location (nm)")
    ax.set_xlabel("Y location (nm)")
    ax.set_title(title)
    fig.legend(loc="upper left")
    fig.tight_layout()
    return pl


def plot_evaluation_data(results):
    jaccard = list()
    hausdorff = list()
    gromov = list()
    rsme = list()
    jaccard_opt = list()
    hausdorff_opt = list()
    x_axis = list()

    for result in results:
        x_axis.append((float(result[0])))
        jaccard.append((float(result[1])))
        hausdorff.append((float(result[2])))
        # gromov.append(result[2])
        rsme.append((float(result[3])))
        jaccard_opt.append((float(result[4])))
        hausdorff_opt.append((float(result[5])))


    fig1, axes = pl.subplots(2)
    ax0, ax1 = axes.flatten()
    ax0.plot(x_axis, jaccard)
    ax0.set_title("Jaccard")
    ax1.plot(x_axis, hausdorff)
    ax1.set_title("Hausdorff")
    # ax2.plot(gromov, x_axis)
    # ax2.set_title("Gromov-Wasserstein")

    fig2, ax2 = pl.subplots(1)
    ax2.plot(x_axis, rsme)
    ax2.set_title("RSME (Optimal mapping) ")

    fig3, axes = pl.subplots(2)
    ax3, ax4 = axes.flatten()
    ax3.plot(x_axis, jaccard_opt)
    ax3.set_title("Jaccard (Optimal mapping)")
    ax4.plot(x_axis, hausdorff_opt)
    ax4.set_title("Hausdorff (Optimal mapping)")
    fig1.tight_layout()
    fig2.tight_layout()
    fig3.tight_layout()
    return fig1, fig2, fig3


def cartesian_rsme(actual, predicted):
    total = 0.0
    for i in range(len(actual)):
        distance = euclidean(actual[i], predicted[i])
        total = total + distance
    mean = total / len(actual)
    return mean


def jaccard_index(set_1, set_2, threshold=0, optimised=False):
    if optimised:
        intersection = optimised_intersection(set_1, set_2, threshold)
    else:
        intersection = calculate_intersection(set_1, set_2, threshold)
    union_cardinality = len(set_1) + len(set_2) - intersection
    return intersection / float(union_cardinality)


def calculate_intersection(set_1, set_2, threshold=0):
    counter = 0
    for coord in set_1:
        best = inf
        best_coord = tuple()
        for coord_2 in set_2:
            ec_distance = euclidean(coord, coord_2)
            if ec_distance <= threshold:
                if ec_distance < best:
                    best_coord = coord_2
        if not len(best_coord) == 0:
            counter += 1
    return counter


def optimised_intersection(set_1, set_2, threshold=0):
    number = 0
    assert len(set_1) == len(set_2)
    for i in range(len(set_1)):
        if euclidean(set_1[i], set_2[i]) < threshold:
            number += 1
    return number


def hausdorff_distance(set_1, set_2):
    dist_lst = []
    for coord in set_1:
        shortest = inf
        for coord_2 in set_2:
            distance = euclidean(coord, coord_2)
            if distance < shortest:
                shortest = distance
        dist_lst.append(shortest)
    return max(dist_lst)


def gromov_wasserstein_distance(set_1, set_2):
    """http://pot.readthedocs.io/en/stable/auto_examples/plot_gromov.html"""
    arr_1 = _generate_arr(set_1)
    arr_2 = _generate_arr(set_2)
    C1 = scipy.spatial.distance.cdist(arr_1, arr_1)
    C2 = scipy.spatial.distance.cdist(arr_2, arr_2)
    C1 /= C1.max()
    C2 /= C2.max()
    # pl.figure()
    # pl.subplot(121)
    # pl.imshow(C1)
    # pl.subplot(122)
    # pl.imshow(C2)
    # pl.show()
    p = ot.unif(len(arr_1))
    q = ot.unif(len(arr_2))
    gw_dist = ot.gromov_wasserstein2(C1, C2, p, q, 'square_loss', epsilon=5e-4)
    # pl.figure()
    # pl.imshow(gw, cmap='jet')
    # pl.colorbar()
    # pl.show()
    return gw_dist


def _generate_arr(set):
    arr = np.zeros((len(set), len(next(iter(set)))), dtype=float)
    row_counter = 0
    for item in set:
        column_counter = 0
        for i in item:
            arr[row_counter][column_counter] = i
            column_counter = column_counter + 1
        row_counter = row_counter + 1
    return arr


def minimum_mapping_metric(set_1, set_2):
    """https://www.cv-foundation.org/openaccess/content_cvpr_2014/papers/Gardner_Measuring_Distance_Between_2014_CVPR_
    paper.pdf"""
    set_1_len = len(set_1)
    set_2_len = len(set_2)
    mu = max(set_1_len, set_2_len)
    muprime = min(set_1_len, set_2_len)
    optimal_mapping = get_optimal_mapping(set_1, set_2)
    list_1 = list(set_1)
    list_2 = list(set_2)
    set_1_opt = optimal_mapping[0]
    set_2_opt = optimal_mapping[1]
    a = list_1[set_1_opt[0]]
    psi_a = list_2[set_2_opt[0]]
    integration = integrate.quad(dirac_a, 10, np.inf, args=(a, psi_a))


def dirac_a(a, psi_a, set_1):
    bracket = euclidean_distance(a, psi_a) / len(set_1)
    return min(1, bracket)


def get_optimal_mapping(set_1, set_2):
    # cost_matrix = ot.dist(arr_1, arr_2)
    cost_matrix = generate_cost_matrix(set_1, set_2)
    arr_1 = _generate_arr(set_1)
    arr_2 = _generate_arr(set_2)
    cost_matrix /= cost_matrix.max()
    opt = optimize.linear_sum_assignment(cost_matrix)
    arr_1_opt_indices = opt[0]
    arr_2_opt_indices = opt[1]
    arr_1_opt = np.zeros((len(arr_1_opt_indices), 2), dtype=float)
    arr_2_opt = np.zeros((len(arr_1_opt_indices), 2), dtype=float)
    for i in range(len(arr_1_opt_indices)):
        arr_1_opt[i] = arr_1[arr_1_opt_indices[i]]
        arr_2_opt[i] = arr_2[arr_2_opt_indices[i]]
    # for i in range(len(arr_1_opt)):
    #     print("{} : {}".format(arr_1_opt[i], arr_2_opt[i]))
    return arr_1_opt, arr_2_opt


def sinkhorn_mapping(set_1, set_2):
    """http://pot.readthedocs.io/en/stable/auto_examples/plot_OT_2D_samples.html"""
    a, b = np.ones((len(set_1),)) / len(set_1), np.ones((len(set_2),)) / len(set_2)
    arr_1 = _generate_arr(set_1)
    arr_2 = _generate_arr(set_2)
    switch_to_cartesian(arr_1, 80 * 400)
    switch_to_cartesian(arr_2, 80 * 400)
    M = ot.dist(arr_1, arr_2)
    G0 = ot.emd(a, b, M)
    counter = 0
    for i in G0:
        inner_counter = 0
        for j in i:
            if j > 0.003:
                print(arr_1[counter], arr_2[inner_counter], j, sep=", ")
            inner_counter += 1
        counter += 1
    pl.figure(4)
    for i in range(arr_1.shape[0]):
        for j in range(arr_2.shape[0]):
            if G0[i, j] > 0.003:
                pl.plot([arr_1[i, 0], arr_2[j, 0]], [arr_1[i, 1], arr_2[j, 1]])
    pl.plot(arr_1[:, 0], arr_1[:, 1], '+b', label='Source samples')
    pl.plot(arr_2[:, 0], arr_2[:, 1], 'xr', label='Target samples')
    pl.show()


def switch_to_cartesian(arr, nm_height):
    for i in arr:
        i[1] = nm_height - i[1]


def generate_cost_matrix(set_1, set_2):
    cost_matrix = np.zeros((len(set_1), len(set_2)))
    row_counter = 0
    column_counter = 0
    for coord in set_1:
        for coord_2 in set_2:
            distance = euclidean(coord, coord_2)
            cost_matrix[row_counter][column_counter] = cost(set_1, set_2, distance)
            column_counter += 1
        column_counter = 0
        row_counter += 1
    return cost_matrix


def cost(set_1, set_2, distance):
    return min(1, distance / len(set_1)) + min(1, distance / len(set_2)) + min(1, distance / len(
        set.union(*[set_1, set_2])))


def euclidean_distance(coordinate, other_coordinate):
    x_sq = pow((coordinate[0] - other_coordinate[0]), 2)
    y_sq = pow((coordinate[1] - other_coordinate[1]), 2)
    return sqrt(x_sq + y_sq)


if __name__ == "__main__":
    main()

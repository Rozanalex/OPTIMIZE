import java.util.Arrays;
import java.util.Scanner;

/**
 * This class models and solves a transportation problem using different methods such as
 * North West Corner Method, Vogel's Approximation Method, and Russell's Approximation Method.
 */
public class TransportationProblem {

    // Instance variables to hold the supply, demand, cost, and solutions for each method
    private int[] supply;
    private int[] demand;
    private int[][] cost;
    private int[][] northWestCornerSolution;
    private int[][] vogelsApproximationSolution;
    private int[][] russellsApproximationSolution;

    /**
     * Constructs a new instance of the transportation problem.
     *
     * @param supply The supply array.
     * @param demand The demand array.
     * @param cost   The cost matrix.
     */
    public TransportationProblem(int[] supply, int[] demand, int[][] cost) {
        this.supply = Arrays.copyOf(supply, supply.length);
        this.demand = Arrays.copyOf(demand, demand.length);
        this.cost = cost;
    }

    /**
     * Implements the North West Corner Method for solving the transportation problem.
     *
     * @return The transportation solution as a matrix.
     */
    private int[][] northWestCornerMethod() {
        int[] tempSupply = Arrays.copyOf(supply, supply.length);
        int[] tempDemand = Arrays.copyOf(demand, demand.length);

        int[][] solution = new int[supply.length][demand.length];
        int i = 0, j = 0;

        while (i < tempSupply.length && j < tempDemand.length) {
            int min = Math.min(tempSupply[i], tempDemand[j]);
            solution[i][j] = min;
            tempSupply[i] -= min;
            tempDemand[j] -= min;

            if (tempSupply[i] == 0) i++;
            if (tempDemand[j] == 0) j++;
        }
        return solution;
    }

    /**
     * Implements Vogel's Approximation Method for solving the transportation problem.
     *
     * @return The transportation solution as a matrix.
     */
    private int[][] vogelsApproximationMethod() {
        int[][] solution = new int[supply.length][demand.length];
        boolean[] rowDone = new boolean[supply.length];
        boolean[] colDone = new boolean[demand.length];
        int[] localSupply = Arrays.copyOf(supply, supply.length);
        int[] localDemand = Arrays.copyOf(demand, demand.length);

        while (!allDone(rowDone, colDone)) {
            int[] maxPenaltyIndex = findMaxPenaltyIndex(rowDone, colDone, localSupply, localDemand);
            int index = maxPenaltyIndex[0];
            boolean isRow = maxPenaltyIndex[1] == 1;

            if (isRow) {
                int minCostCol = findMinCostCol(index, localDemand, colDone);
                int quantity = Math.min(localSupply[index], localDemand[minCostCol]);
                solution[index][minCostCol] = quantity;
                updateSupplyDemand(localSupply, localDemand, index, minCostCol, quantity, rowDone, colDone);
            } else {
                int minCostRow = findMinCostRow(index, localSupply, rowDone);
                int quantity = Math.min(localSupply[minCostRow], localDemand[index]);
                solution[minCostRow][index] = quantity;
                updateSupplyDemand(localSupply, localDemand, minCostRow, index, quantity, rowDone, colDone);
            }
        }
        return solution;
    }

    /**
     * Implements Russell's Approximation Method for solving the transportation problem.
     *
     * @return The transportation solution as a matrix.
     */
    private int[][] russellsApproximationMethod() {
        int[][] allocation = new int[supply.length][demand.length];
        boolean[] rowCompleted = new boolean[supply.length];
        boolean[] colCompleted = new boolean[demand.length];
        int[] localSupply = Arrays.copyOf(supply, supply.length);
        int[] localDemand = Arrays.copyOf(demand, demand.length);

        while (!allCompleted(rowCompleted, colCompleted)) {
            int[] maxSavingCell = findMaxSavingCell(rowCompleted, colCompleted, localSupply, localDemand);
            if (maxSavingCell[0] == -1) {
                break;
            }

            int row = maxSavingCell[0];
            int col = maxSavingCell[1];
            int shipment = Math.min(localSupply[row], localDemand[col]);
            allocation[row][col] = shipment;
            updateSupplyDemand(localSupply, localDemand, row, col, shipment, rowCompleted, colCompleted);
        }

        return allocation;
    }

    /**
     * Solves the transportation problem using all the implemented methods and prints the solutions.
     */
    public void solve() {
        if (!isBalanced()) {
            System.out.println("The problem is not balanced!");
            return;
        }

        printInitialTable();

        northWestCornerSolution = northWestCornerMethod();
        vogelsApproximationSolution = vogelsApproximationMethod();
        russellsApproximationSolution = russellsApproximationMethod();

        System.out.println("North-West Corner method");
        System.out.println("Total distribution cost: " + calculateTotalCost(northWestCornerSolution, cost));
        printSolution(northWestCornerSolution);

        System.out.println("Vogel's approximation");
        System.out.println("Total distribution cost: " + calculateTotalCost(vogelsApproximationSolution, cost));
        printSolution(vogelsApproximationSolution);

        System.out.println("Russell's approximation");
        System.out.println("Total distribution cost: " + calculateTotalCost(russellsApproximationSolution, cost));
        printSolution(russellsApproximationSolution);
    }

    /**
     * Prints the solution matrix along with the supply and demand.
     *
     * @param solution The distribution matrix representing the solution.
     */
    private void printSolution(int[][] solution) {
        if (solution == null || solution.length == 0 || solution[0].length == 0) {
            System.out.println("No solution available.");
            return;
        }

        System.out.println("Solution:");
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[i].length; j++) {
                System.out.printf("%5d ", solution[i][j]);
            }
            System.out.printf("| %5d\n", supply[i]);
        }

        for (int i = 0; i < solution[0].length; i++) {
            System.out.print("-----");
        }
        System.out.println("---------");

        for (int j = 0; j < demand.length; j++) {
            System.out.printf("%5d ", demand[j]);
        }
        System.out.println("\n");
    }

    /**
     * Prints the initial transportation problem matrix.
     */
    private void printInitialTable() {
        System.out.println("Transportation problem:");
        for (int i = 0; i < cost.length; i++) {
            for (int j = 0; j < cost[i].length; j++) {
                System.out.printf("%5d ", cost[i][j]);
            }
            System.out.printf("| %5d\n", supply[i]);
        }

        for (int i = 0; i < cost[0].length; i++) {
            System.out.print("-----");
        }
        System.out.println("---------");

        for (int j = 0; j < demand.length; j++) {
            System.out.printf("%5d ", demand[j]);
        }
        System.out.println("\n");
    }

    /**
     * Checks if the transportation problem is balanced (total supply equals total demand).
     *
     * @return true if balanced, false otherwise.
     */
    public boolean isBalanced() {
        int totalSupply = 0;
        int totalDemand = 0;

        for (int s : supply) totalSupply += s;
        for (int d : demand) totalDemand += d;

        return totalSupply == totalDemand;
    }

    /**
     * Finds the cell with the maximum saving in the cost matrix.
     *
     * @param rowCompleted Array indicating if rows are completed.
     * @param colCompleted Array indicating if columns are completed.
     * @param supply       Array of supply values.
     * @param demand       Array of demand values.
     * @return An array containing the indices of the selected row and column.
     */
    private int[] findMaxSavingCell(boolean[] rowCompleted, boolean[] colCompleted, int[] supply, int[] demand) {
        int maxSaving = Integer.MIN_VALUE;
        int selectedRow = -1;
        int selectedCol = -1;

        // Calculate savings for each unallocated cell
        for (int i = 0; i < supply.length; i++) {
            for (int j = 0; j < demand.length; j++) {
                if (!rowCompleted[i] && !colCompleted[j]) {
                    int saving = calculateSaving(i, j, rowCompleted, colCompleted);
                    if (saving > maxSaving) {
                        maxSaving = saving;
                        selectedRow = i;
                        selectedCol = j;
                    }
                }
            }
        }

        return new int[]{selectedRow, selectedCol};
    }

    /**
     * Calculates the saving for a given cell in the cost matrix.
     *
     * @param row          The row index of the cell.
     * @param col          The column index of the cell.
     * @param rowCompleted Array indicating if rows are completed.
     * @param colCompleted Array indicating if columns are completed.
     * @return The calculated saving for the cell.
     */
    private int calculateSaving(int row, int col, boolean[] rowCompleted, boolean[] colCompleted) {
        int rowMin = findMinInRowExcluding(row, col, colCompleted);
        int colMin = findMinInColExcluding(col, row, rowCompleted);
        return (rowMin + colMin) - cost[row][col];
    }

    /**
     * Finds the minimum cost in a row, excluding a specified column.
     *
     * @param row          The row index.
     * @param excludedCol  The column index to exclude.
     * @param colCompleted Array indicating if columns are completed.
     * @return The minimum cost found in the row.
     */
    private int findMinInRowExcluding(int row, int excludedCol, boolean[] colCompleted) {
        int min = Integer.MAX_VALUE;
        for (int j = 0; j < cost[row].length; j++) {
            if (j != excludedCol && !colCompleted[j] && cost[row][j] < min) {
                min = cost[row][j];
            }
        }
        return min;
    }

    /**
     * Finds the minimum cost in a column, excluding a specified row.
     *
     * @param col          The column index.
     * @param excludedRow  The row index to exclude.
     * @param rowCompleted Array indicating if rows are completed.
     * @return The minimum cost found in the column.
     */
    private int findMinInColExcluding(int col, int excludedRow, boolean[] rowCompleted) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < cost.length; i++) {
            if (i != excludedRow && !rowCompleted[i] && cost[i][col] < min) {
                min = cost[i][col];
            }
        }
        return min;
    }

    /**
     * Finds the index with the maximum penalty in either the supply or demand array.
     *
     * @param rowDone Array indicating if rows are done.
     * @param colDone Array indicating if columns are done.
     * @param supply  Array of supply values.
     * @param demand  Array of demand values.
     * @return An array containing the index and a flag indicating if it's a row or column.
     */
    private int[] findMaxPenaltyIndex(boolean[] rowDone, boolean[] colDone, int[] supply, int[] demand) {
        int maxPenalty = -1;
        int maxIndex = -1;
        boolean isRow = true;

        for (int i = 0; i < supply.length; i++) {
            if (!rowDone[i]) {
                int penalty = calculateRowPenalty(i, colDone);
                if (penalty > maxPenalty) {
                    maxPenalty = penalty;
                    maxIndex = i;
                    isRow = true;
                }
            }
        }

        for (int j = 0; j < demand.length; j++) {
            if (!colDone[j]) {
                int penalty = calculateColPenalty(j, rowDone);
                if (penalty > maxPenalty) {
                    maxPenalty = penalty;
                    maxIndex = j;
                    isRow = false;
                }
            }
        }

        return new int[]{maxIndex, isRow ? 1 : 0};
    }

    /**
     * Calculates the penalty for a given row by finding the difference between the smallest and second smallest values.
     *
     * @param row     The row index.
     * @param colDone Array indicating if columns are done.
     * @return The calculated penalty for the row.
     */
    private int calculateRowPenalty(int row, boolean[] colDone) {
        int firstMin = Integer.MAX_VALUE;
        int secondMin = Integer.MAX_VALUE;

        for (int j = 0; j < cost[row].length; j++) {
            if (!colDone[j]) {
                if (cost[row][j] < firstMin) {
                    secondMin = firstMin;
                    firstMin = cost[row][j];
                } else if (cost[row][j] < secondMin) {
                    secondMin = cost[row][j];
                }
            }
        }

        return (firstMin == Integer.MAX_VALUE || secondMin == Integer.MAX_VALUE) ? 0 : secondMin - firstMin;
    }

    /**
     * Calculates the penalty for a given column by finding the difference between the smallest and second smallest values.
     *
     * @param col      The column index.
     * @param rowDone  Array indicating if rows are done.
     * @return The calculated penalty for the column.
     */
    private int calculateColPenalty(int col, boolean[] rowDone) {
        int firstMin = Integer.MAX_VALUE;
        int secondMin = Integer.MAX_VALUE;

        for (int i = 0; i < cost.length; i++) {
            if (!rowDone[i]) {
                if (cost[i][col] < firstMin) {
                    secondMin = firstMin;
                    firstMin = cost[i][col];
                } else if (cost[i][col] < secondMin) {
                    secondMin = cost[i][col];
                }
            }
        }

        return (firstMin == Integer.MAX_VALUE || secondMin == Integer.MAX_VALUE) ? 0 : secondMin - firstMin;
    }

    /**
     * Finds the column with the minimum cost for a given row.
     *
     * @param row      The row index.
     * @param demand   Array of demand values.
     * @param colDone  Array indicating if columns are done.
     * @return The index of the column with the minimum cost.
     */
    private int findMinCostCol(int row, int[] demand, boolean[] colDone) {
        int minCost = Integer.MAX_VALUE;
        int minCostCol = -1;
        for (int j = 0; j < demand.length; j++) {
            if (!colDone[j] && cost[row][j] < minCost) {
                minCost = cost[row][j];
                minCostCol = j;
            }
        }
        return minCostCol;
    }

    /**
     * Finds the row with the minimum cost for a given column.
     *
     * @param col      The column index.
     * @param supply   Array of supply values.
     * @param rowDone  Array indicating if rows are done.
     * @return The index of the row with the minimum cost.
     */
    private int findMinCostRow(int col, int[] supply, boolean[] rowDone) {
        int minCost = Integer.MAX_VALUE;
        int minCostRow = -1;
        for (int i = 0; i < supply.length; i++) {
            if (!rowDone[i] && cost[i][col] < minCost) {
                minCost = cost[i][col];
                minCostRow = i;
            }
        }
        return minCostRow;
    }

    /**
     * Updates the supply and demand arrays based on the allocated quantity and marks rows or columns as done if needed.
     *
     * @param supply   Array of supply values.
     * @param demand   Array of demand values.
     * @param row      The row index of the allocation.
     * @param col      The column index of the allocation.
     * @param quantity The quantity to be allocated.
     * @param rowDone  Array indicating if rows are done.
     * @param colDone  Array indicating if columns are done.
     */
    private void updateSupplyDemand(int[] supply, int[] demand, int row, int col, int quantity, boolean[] rowDone, boolean[] colDone) {
        supply[row] -= quantity;
        demand[col] -= quantity;

        if (supply[row] == 0) rowDone[row] = true;
        if (demand[col] == 0) colDone[col] = true;
    }

    /**
     * Checks if all rows and columns are completed.
     *
     * @param rowDone  Array indicating if rows are done.
     * @param colDone  Array indicating if columns are done.
     * @return true if all rows and columns are completed, false otherwise.
     */

    private boolean allDone(boolean[] rowDone, boolean[] colDone) {
        for (boolean b : rowDone) if (!b) return false;
        for (boolean b : colDone) if (!b) return false;
        return true;
    }

    /**
     * Calculates the total cost of a given distribution solution.
     *
     * @param distribution The distribution matrix representing the solution.
     * @param cost         The cost matrix.
     * @return The total cost of the distribution.
     */
    private int calculateTotalCost(int[][] distribution, int[][] cost) {
        int totalCost = 0;
        for (int i = 0; i < distribution.length; i++) {
            for (int j = 0; j < distribution[i].length; j++) {
                totalCost += distribution[i][j] * cost[i][j];
            }
        }
        return totalCost;
    }

    /**
     * Checks if all rows and columns are completely allocated in the distribution matrix.
     *
     * @param rowCompleted Array indicating if rows are completed.
     * @param colCompleted Array indicating if columns are completed.
     * @return true if all allocations are completed, false otherwise.
     */
    private boolean allCompleted(boolean[] rowCompleted, boolean[] colCompleted) {
        for (boolean b : rowCompleted) if (!b) return false;
        for (boolean b : colCompleted) if (!b) return false;
        return true;
    }

    /**
     * The main method to run the transportation problem solver.
     * It reads input for supply, demand, and cost, and then solves the problem.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the number of supply points: ");
        int supplySize = scanner.nextInt();
        int[] supply = new int[supplySize];

        System.out.println("Enter the supply values:");
        for (int i = 0; i < supplySize; i++) {
            supply[i] = scanner.nextInt();
        }

        System.out.print("Enter the number of demand points: ");
        int demandSize = scanner.nextInt();
        int[] demand = new int[demandSize];

        System.out.println("Enter the demand values:");
        for (int i = 0; i < demandSize; i++) {
            demand[i] = scanner.nextInt();
        }

        int[][] cost = new int[supplySize][demandSize];
        System.out.println("Enter the cost matrix:");
        for (int i = 0; i < supplySize; i++) {
            for (int j = 0; j < demandSize; j++) {
                cost[i][j] = scanner.nextInt();
            }
        }

        TransportationProblem problem = new TransportationProblem(supply, demand, cost);
        problem.solve();
    }
}
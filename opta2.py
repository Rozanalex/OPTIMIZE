import numpy as np

def input_vector(prompt):
    """Read a vector from user input and return it as a NumPy array."""
    return np.array(list(map(float, input(prompt).split())))

def input_matrix(prompt, rows):
    """Read a matrix from user input and return it as a NumPy array."""
    print(prompt)
    matrix = []
    for _ in range(rows):
        row = list(map(float, input().split()))
        matrix.append(row)
    
    return np.array(matrix)

def method(x0, alpha, eps, b, C, A):
    sol = True
    for row in A:
        neg = True
        for value in row:
            if value > 0:
                neg = False;
        if neg:
            sol = False
    if not sol:
         print ("The problem does not have solution")
         return
    bound = A @ x0.transpose()
    for i in range(A.shape[0]):
        a = bound.flatten()[i]
        if bound.flatten()[i] > b[i]:
            print("The method is not applicable")
            return
    x = np.array(x0, dtype=float)
    solved = False
    counter = 0
    while not solved:
        counter+=1
        D = np.diag(x)  # Diagonal matrix of current solution
        A_tilda = A @ D  # Modified constraint matrix
        c_tilda = D @ C  # Modified cost coefficients
        A_rev = A_tilda @ A_tilda.T
        mult = A_tilda.T @ np.linalg.inv(A_tilda @ A_tilda.transpose()) @ A_tilda  # Multiplier for the search direction
        P = np.identity(mult.shape[0]) - mult  # Projection matrix
        c_p = P @ c_tilda  # Reduced cost coefficients
        minimum = np.min(c_p)  # Find minimum reduced cost
        #print(counter)
        if minimum >= 0.1:
            print("The method is not applicable!")  # Condition for applicability
            return
        v = np.abs(minimum)
        x_tilda = 1 + alpha * c_p / v  # Update direction of movement

        x_star = D @ x_tilda  # New candidate solution
        if (counter == 25):
            print(123)
        da = np.linalg.norm(np.subtract(x_star,x), ord=2)
        if np.linalg.norm(np.subtract(x_star,x), ord=2) <= eps:  # Check for convergence
            print(f"Solution for α = {alpha}: {x_star}")
            objective_value = C @ x_star  # Calculate objective function value
            print(f"Objective function value: {objective_value}")
            solved = True
            return
        x = x_star  # Update current solution
    print("The problem does not have a solution!")  # No solution found

def simplex_method_manual(C, A, b):

    num_vars = len(C)  # Number of decision variables
    num_constraints = len(b)  # Number of constraints

    # Create the simplex tableau
    tableau = np.zeros((num_constraints + 1, num_vars + num_constraints + 1))
    tableau[:-1, :num_vars] = A  # Fill in constraints
    tableau[:-1, num_vars:num_vars + num_constraints] = np.eye(num_constraints)  # Identity matrix for slack variables
    tableau[:-1, -1] = b  # Right-hand side values
    tableau[-1, :num_vars] = -C  # Objective function coefficients (inverted)

    # Iterate through the Simplex method
    while True:
        # Step 1: Choose entering variable
        pivot_col = np.argmin(tableau[-1, :-1])  # Column with most negative cost
        if tableau[-1, pivot_col] >= 0:
            break

        # Step 2: Choose leaving variable
        ratios = []  # To hold ratios for determining the pivot row
        for i in range(num_constraints):
            if tableau[i, pivot_col] > 0:
                ratios.append(tableau[i, -1] / tableau[i, pivot_col])  # Calculate ratio
            else:
                ratios.append(np.inf)  # Infinite if non-positive

        pivot_row = np.argmin(ratios)  # Find row for leaving variable
        if ratios[pivot_row] == np.inf:
            print("The problem does not have a finite solution.")  # No feasible solution
            return

        # Step 3: Pivot to update tableau
        pivot_value = tableau[pivot_row, pivot_col]  # Pivot element
        tableau[pivot_row, :] /= pivot_value  # Normalize pivot row

        for i in range(num_constraints + 1):
            if i != pivot_row:
                tableau[i, :] -= tableau[i, pivot_col] * tableau[pivot_row, :]  # Update other rows

        # Debugging output to check updated tableau

    # Extracting the optimal solution
    solution = np.zeros(num_vars)
    for j in range(num_vars):
        # Check if each variable is basic
        basic_vars = tableau[:, j]
        if np.sum(basic_vars == 1) == 1 and np.sum(basic_vars > 1) == 0:
            # If basic, take corresponding value from right-hand side
            row_index = np.where(basic_vars == 1)[0][0]
            solution[j] = tableau[row_index, -1]

    objective_value = tableau[-1, -1]  # Correct value for maximization
    print("Optimal solution x:", solution)
    print("Objective function value:", objective_value)

# Main part of the program
if __name__ == "__main__":
    # Input coefficients for the objective function
    C = input_vector("C (coefficients of objective function): ")
    num_constraints = int(input("Number of constraints (n): "))
    A = input_matrix("A (constraints matrix): ", num_constraints)
    x0 = input_vector("x0 (initial guess): ")
    b = input_vector("b (right-hand side of constraints): ")
    eps = float(input("eps (tolerance): "))

    # Execute the Interior-Point Method with α = 0.5 and α = 0.9
    print("\nRunning Interior-Point Method for α = 0.5:")
    method(x0, 0.5, eps, b, C, A)
    
    print("\nRunning Interior-Point Method for α = 0.9:")
    method(x0, 0.9, eps, b, C, A)

    # Execute the Simplex Method
    print("\n----------")
    print("Running Simplex Method:")
    simplex_method_manual(C, A, b)

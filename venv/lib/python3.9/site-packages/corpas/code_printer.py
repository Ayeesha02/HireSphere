import os

def stema(filename):
    """
    Opens the file with the given filename (relative to the package directory)
    and prints its content.
    """
    # Determine the package's directory (assumes code_printer.py is in the package)
    package_dir = os.path.dirname(__file__)
    
    # Construct the full path. If filename is already absolute, os.path.join won't change it.
    file_path = os.path.join(package_dir, filename)
    
    try:
        with open(file_path, 'r') as f:
            code = f.read()
        print(code)
    except Exception as e:
        print(f"Error reading file: {e}")

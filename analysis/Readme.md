Sample Module Repository
========================

This simple project is an example repo for Python projects.

---------------

Install package
````
pip install -r requirements.txt
````


Build package
````
python setup.py sdist
````

Test Package
````
pytest tests
````

Run Pacakge
export PYTHONPATH=.
python HKMA/CreateFakeTrade.py -o bondtrade2.avro
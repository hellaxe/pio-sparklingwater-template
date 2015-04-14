"""
Import data for H2O Sparkling Water Engine.
"""

import predictionio
import argparse
import csv

def import_events(client, file):
    f = open(file, 'r')
    csvFile = csv.reader(f)

    print "Importing electric load data..."

    # Skip header line
    header = csvFile.next()

    for idx, row in enumerate(csvFile):
        print "Importing data from row ",idx
        for i in xrange(1,len(row)):
            if (row[i] != ''):
                circuitId = header[i]
                time = row[0]
                energy = row[i]

                client.create_event(
                    event = 'predict_energy',
                    entity_type = 'electrical_load',
                    entity_id = circuitId,
                    properties = {
                        'circuitId': circuitId,
                        'time': time,
                        'energy': energy
                        }
                    )
    
    f.close()

    print "Done importing data."

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
            description="Import energy load data for engine")
    parser.add_argument('--access_key', default='invald_access_key')
    parser.add_argument('--url', default="http://localhost:7070")
    parser.add_argument('--file', default="./data/sample_data.csv")

    args = parser.parse_args()
    print args

    client = predictionio.EventClient(
        access_key=args.access_key,
        url=args.url,
        threads=5,
        qsize=500)
    import_events(client, args.file)

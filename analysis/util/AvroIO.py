import avro.schema
from avro.datafile import  DataFileWriter
from avro.io import DatumWriter
import abc
class Writer(abc.ABC):
    @abc.abstractmethod
    def write(self, obj):
        pass

class AvroFileWriter(Writer):
    def __init__(self, schemaFile, avroFile):
        self.schema = avro.schema.Parse(open(schemaFile, "rb").read())
        self.writer = DataFileWriter(open(avroFile, "wb"), DatumWriter(), self.schema)
    def write(self, obj):
        self.writer.append(obj);

    def close(self):
        self.writer.close()
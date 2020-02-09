import abc

class DataAdapter(abc.ABC):
    @abc.abstractmethod
    def apply(self, json):
        pass
module VotingSystem
{
    interface Callback {
        void reportResponse(string response);
    }

    interface VotingService {
        void registerObserver(Callback* callback);
        void unregisterObserver(Callback* callback);
        bool registerVoter(string voterId, Callback* callback);
        void unRegisterVoter(string voterId);
        void listVotingStations(string city, Callback* callback);
        void getVotingStation(string voterId, Callback* callback);
        void uploadVoterFile(string filePath, Callback* callback);
    }
}

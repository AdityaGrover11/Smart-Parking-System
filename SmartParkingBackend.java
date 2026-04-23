
import java.time.LocalDateTime;
import java.time.Duration;


public class SmartParkingBackend {

    public int totalFloors = 3;
    public int slotsPerFloor = 7;

    public String[][] parkedVehicles = new String[totalFloors][slotsPerFloor];
    public String[][] vehicleTypes = new String[totalFloors][slotsPerFloor];
    public LocalDateTime[][] entryTimes = new LocalDateTime[totalFloors][slotsPerFloor];

    public long revenueToday = 0;       
    public int currentOccupancy = 0;    
    public int totalVehiclesToday = 0;  

    private DatabaseManager dbManager; 

    public SmartParkingBackend() {
        dbManager = new DatabaseManager();
        
        // Load statistics
        long[] stats = dbManager.loadStats();
        revenueToday = stats[0];
        totalVehiclesToday = (int) stats[1];

        dbManager.loadAllVehicles(parkedVehicles, vehicleTypes, entryTimes);

        // Recalculate occupancy from loaded data
        for (int f = 0; f < totalFloors; f++) {
            for (int s = 0; s < slotsPerFloor; s++) {
                if (parkedVehicles[f][s] != null) {
                    currentOccupancy++;
                }
            }
        }
    }

    // Check if vehicle exists in lot
    public boolean isVehicleParked(String vehicleNumber) {
        for (int f = 0; f < totalFloors; f++) {
            for (int s = 0; s < slotsPerFloor; s++) {
                if (vehicleNumber.equals(parkedVehicles[f][s])) {
                    return true;
                }
            }
        }
        return false;
    }

    // Find a vehicle's exact location, returns {floor, slot} or null
    public int[] findVehicleLocation(String vehicleNumber) {
        for (int f = 0; f < totalFloors; f++) {
            for (int s = 0; s < slotsPerFloor; s++) {
                if (vehicleNumber.equals(parkedVehicles[f][s])) {
                    return new int[]{f, s};
                }
            }
        }
        return null;
    }

    // Park a vehicle into the first empty slot
    public boolean parkVehicle(String vehicleNumber, String type) {
        if (currentOccupancy >= totalFloors * slotsPerFloor) {
            return false; // Parking is full
        }

        for (int f = 0; f < totalFloors; f++) {
            for (int s = 0; s < slotsPerFloor; s++) {
                if (parkedVehicles[f][s] == null) {
                    
                    parkedVehicles[f][s] = vehicleNumber;
                    vehicleTypes[f][s] = type;
                    entryTimes[f][s] = LocalDateTime.now();
                    
                    currentOccupancy++;     
                    totalVehiclesToday++;   

                    dbManager.saveVehicle(f, s, vehicleNumber, type, entryTimes[f][s]);
                    dbManager.updateStats(revenueToday, totalVehiclesToday);
                    
                    return true;            
                }
            }
        }
        return false;
    }

    // Calculate dynamic fee, log history, and free up the given slot
    public long calculateFeeAndRemoveVehicle(int floor, int slot) {
        LocalDateTime exitTime = LocalDateTime.now();
        long hoursSpent = Duration.between(entryTimes[floor][slot], exitTime).toHours();
        
        if (hoursSpent == 0) {
            hoursSpent = 1; // Minimum 1 hr charge
        }

        int hourlyRate = 0;
        String type = vehicleTypes[floor][slot];
        
        if (type.equals("Bike")) {
            hourlyRate = 10;
        } else if (type.equals("Car")) {
            hourlyRate = 20;
        } else {
            hourlyRate = 40; 
        }

        long totalFee = hoursSpent * hourlyRate;
        revenueToday += totalFee;

        dbManager.logHistory(parkedVehicles[floor][slot], type, entryTimes[floor][slot], exitTime, totalFee);

        parkedVehicles[floor][slot] = null;
        vehicleTypes[floor][slot] = null;
        entryTimes[floor][slot] = null;
        currentOccupancy--;

        dbManager.removeVehicle(floor, slot);
        dbManager.updateStats(revenueToday, totalVehiclesToday);

        return totalFee;
    }

    // Fetch formatted parking history
    public java.util.List<String[]> getHistory(String search, String timeFilter) {
        return dbManager.queryHistory(search, timeFilter);
    }
}

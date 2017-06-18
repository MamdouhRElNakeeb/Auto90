<?php
/**
 * Created by PhpStorm.
 * User: mamdouhelnakeeb
 * Date: 6/12/17
 * Time: 12:21 PM
 */

$latStart = htmlentities($_REQUEST["latStart"]);
$lonStart = htmlentities($_REQUEST["lonStart"]);
$latEnd = htmlentities($_REQUEST["latEnd"]);
$lonEnd = htmlentities($_REQUEST["lonEnd"]);
$regionPick = htmlentities($_REQUEST["regionPick"]);
$regionDist = htmlentities($_REQUEST["regionDist"]);
$gender = htmlentities($_REQUEST["gender"]);
$daysStr = htmlentities($_REQUEST["daysStr"]);
$timeStart = htmlentities($_REQUEST["timeStart"]);
$timeEnd = htmlentities($_REQUEST["timeEnd"]);


if (empty($latStart) || empty($lonStart) || empty($latEnd) || empty($lonEnd)
    || empty($regionPick) || empty($regionDist) || empty($gender) || empty($timeStart)
    || empty($timeEnd) || empty($daysStr)){

    $returnArray["status"] = "400";
    $returnArray["message"] = "Missing Fields!";
    echo json_encode($returnArray);
    return;
}

require ("secure/access.php");
require ("secure/Auto90Conn.php");

$access = new access(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
$access->connect();

$daysArray = explode(',', $daysStr);

$result = $access->getRides($regionPick, $regionDist, $gender, $daysArray, $timeStart, $timeEnd);

$a = array();
$b = array();

if ($result){
    $returnArray["error"] = FALSE;


    if (mysqli_num_rows($result) >=1){

        $returnArray["message"] = "Available rides";
        $returnArray["type"] = "ride";

        $returnArray["rideID"] = $result["id"];
        $returnArray["latStart"] = $result["latStart"];
        $returnArray["lonStart"] = $result["lonStart"];
        $returnArray["latEnd"] = $result["latEnd"];
        $returnArray["lonEnd"] = $result["lonEnd"];
        $returnArray["driverID"] = $result["driverID"];
        $returnArray["riders"] = $result["riders"];

        $fare = $access->getFare($result["riders"]);

        $returnArray["fare"] = $fare["kmFare"];

    /*
        while ($row = mysqli_fetch_array($result)) {
            $b["rideID"] = $row["id"];
            $b["latStart"] = $row["latStart"];
            $b["lonStart"] = $row["lonStart"];
            $b["latEnd"] = $row["latEnd"];
            $b["lonEnd"] = $row["lonEnd"];
            $b["driverID"] = $row["driverID"];
            $b["riders"] = $row["riders"];

            array_push($a, $b);
        }
*/


    }
    else {

        $access->insertRide($latStart, $lonStart, $latEnd, $lonEnd, $regionPick, $regionDist, $gender, $daysStr, $gender, $timeStart, $timeEnd);

        $resultDrivers = $access->getDrivers($regionPick, $regionDist);

        if ($resultDrivers !=false){
            $no_of_users = mysqli_num_rows($resultDrivers);
        }
        else{
            $no_of_users = 0;
        }

        if ($no_of_users > 0) {
            $regIDs = array();
            if(mysqli_num_rows($resultDrivers) > 0 ){
                while ($row = mysqli_fetch_array($resultDrivers)) {
                    $regIDs[] = $row["regID"];
                }

                $newRideJSON = array (

                    'regionPick' 	=> $regionPick,
                    'regionDist'	=> $regionDist,
                    'gender'	    => $gender,
                    'daysStr'       => $daysStr,
                    'timeStart'     => $timeStart,
                    'timeEnd'	    => $timeEnd
                );

                $message = array("newRide" => $newRideJSON);
                $message_status = $access->send_notification($regIDs, $message);

                $returnArray["type"] = "sentToDrivers";

                $returnArray["message"] = "Ride Request Sent";
            }
        }
        else {

            $returnArray["type"] = "noDrivers";
            $returnArray["message"] = "No Drivers or Rides Available now";
        }

    }

    echo json_encode($returnArray, JSON_UNESCAPED_UNICODE);

}
else{
    $returnArray["error"] = TRUE;
    echo json_encode($returnArray);
}

$access->disconnect();

?>
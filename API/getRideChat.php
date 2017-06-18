<?php
/**
 * Created by PhpStorm.
 * User: mamdouhelnakeeb
 * Date: 6/18/17
 * Time: 5:29 AM
 */


$rideID = htmlentities($_REQUEST["rideID"]);

if (empty($rideID) ){

    $returnArray["status"] = "400";
    $returnArray["message"] = "Missing Fields!";
    echo json_encode($returnArray);
    return;
}

require ("secure/access.php");
require ("secure/Auto90Conn.php");

$access = new access(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
$access->connect();


$result = $access->getRideChat($rideID);

$a = array();
$b = array();

if ($result) {
    $returnArray["error"] = FALSE;

    if (mysqli_num_rows($result) >= 1) {

        while ($row = mysqli_fetch_array($result)) {
            $b["msgID"] = $row["id"];
            $b["userID"] = $row["userID"];
            $b["msg"] = $row["msg"];

            array_push($a, $b);
        }

        $returnArray["chat"] = $a;
    }

    echo json_encode($returnArray, JSON_UNESCAPED_UNICODE);
}
else{
    $returnArray["error"] = TRUE;
    echo json_encode($returnArray);
}

$access->disconnect();
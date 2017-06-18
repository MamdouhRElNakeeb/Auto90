<?php
/**
 * Created by PhpStorm.
 * User: Mamdouh El Nakeeb
 * Date: 4/13/17
 * Time: 3:30 PM
 */


class access{
    //connection global variables
    var $host = null;
    var $username = null;
    var $dpass = null;
    var $dname = null;
    var $conn = null;
    var $result = null;

    public function __construct($dbhost, $dbuser, $dbpass, $dbname){

        $this->host = $dbhost;
        $this->username = $dbuser;
        $this->dpass = $dbpass;
        $this->dname = $dbname;
    }

    public function connect(){
        $this->conn = new mysqli($this->host, $this->username, $this->dpass, $this->dname);
        if (mysqli_connect_errno()) {
            echo "Failed to connect to Database: " . mysqli_connect_error();
        }
        $this->conn->set_charset("utf8");
    }

    public function disconnect(){
        if($this->conn != null){
            $this->conn->close();
        }
    }

    public function getTableContent($tableName){

        $sql = "SELECT * FROM $tableName";
        $result = $this->conn->query($sql);
        return $result;

    }

    public function getFare($riders){
        $sql = "SELECT * FROM fare WHERE id= '".$riders."'";
        $result = $this->conn->query($sql);
        return $result;
    }

    // get appropriate rides from database
    public function getRides($regionPick, $regionDist, $gender, $daysArray, $timeStart, $timeEnd){

        $daysStr = "";

        for ($x = 0; $x < sizeof($daysArray); $x++) {
            if ($x===0){
                $daysStr .= "`days` LIKE '%$daysArray[$x]%' ";
            }
            else{
                $daysStr .= "OR `days` LIKE '%$daysArray[$x]%' ";
            }
        }

        $sql = "SELECT * FROM `rides`
                WHERE `regionStart` LIKE '%$regionPick%'
                AND `regionEnd` LIKE '%$regionDist%'
                AND `timeStart`>=$timeStart
                AND `timeEnd`<=$timeEnd
                AND `gender`= '$gender'
                AND `riders`<3
                AND ($daysStr) 
                LIMIT 1";

        $result = $this->conn->query($sql);
        return $result;

    }

    public function getRideChat($rideID){

        $sql = "SELECT * FROM ride_chat_messages WHERE rideID= '".$rideID."'";
        $result = $this->conn->query($sql);
        return $result;
    }

    // insert service into database
    public function insertRide($latStart, $lonStart, $latEnd, $lonEnd, $regionPick, $regionDist, $gender, $daysStr, $timeStart, $timeEnd){


        $sql = "INSERT INTO rides SET latStart=?, lonStart=?, latEnd=?, lonEnd=?, regionStart=?, regionEnd=?, timeStart=?, timeEnd=?, gender=?, days=?";
        $statement = $this->conn->prepare($sql);
        if(!$statement){
            throw new Exception($statement->error);
        }
        // bind 4 parameters of type string to be placed in $sql command
        $statement->bind_param("ssssssssss", $latStart, $lonStart, $latEnd, $lonEnd, $regionPick, $regionDist, $timeStart, $timeEnd, $gender, $daysStr);
        $returnValue = $statement->execute();
        return $returnValue;

    }

    public function updateRide($rideID, $status, $driverID){

        $sql = "UPDATE rides SET status=?, driverID=? WHERE id=?";
        $statement = $this->conn->prepare($sql);
        if(!$statement){
            throw new Exception($statement->error);
        }
        // bind 4 parameters of type string to be placed in $sql command
        $statement->bind_param("sss", $status, $driverID, $rideID);
        $returnValue = $statement->execute();
        return $returnValue;
    }


    // insert user into database
    public function registerUser($name, $email, $password, $salt, $mobile, $regID){
        $result = $this->selectUser($email);
        if ($result){
            return;
        }
        else{
            $sql = "INSERT INTO users SET name=?, email=?, password=?, salt=?, mobile=?, regID=?";
            $statement = $this->conn->prepare($sql);

            if(!$statement){
                throw new Exception($statement->error);
            }
            // bind 9 parameters of type string to be placed in $sql command
            $statement->bind_param("ssssss", $name, $email, $password, $salt, $mobile, $regID);
            $returnValue = $statement->execute();
            return $returnValue;

        }
    }

    // update user from database
    public function updateUser($name, $email, $password, $salt, $mobile, $regID, $userID){
        $sql = "UPDATE users SET name=?, email=?, password=?, salt=?, mobile=?, regID=? WHERE id=?";
        $statement = $this->conn->prepare($sql);
        if(!$statement){
            throw new Exception($statement->error);
        }
        // bind 9 parameters of type string to be placed in $sql command
        $statement->bind_param("sssssss", $name, $email, $password, $salt, $mobile, $regID, $userID);
        $returnValue = $statement->execute();
        return $returnValue;

    }

    // select user form database
    public function selectUser($email){
        $sql = "SELECT * FROM users WHERE email = '".$email."' ";
        $result = $this->conn->query($sql);
        if($result !=null && (mysqli_num_rows($result) >=1)){
            $row = $result->fetch_array(MYSQLI_ASSOC);
            if(!empty($row)){
                $returnArray = $row;
                return $returnArray;
            }
        }
    }


    public function send_notification ($tokens, $message){
        $url = 'https://fcm.googleapis.com/fcm/send';
        $fields = array(
            'registration_ids' => $tokens,
            'data' => $message
        );
        $headers = array(
            'Authorization:key = AIzaSyAqZWohv2TL1Bcj-kHFB7asO-0N6a8PPXI ',
            'Content-Type: application/json'
        );
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt ($ch, CURLOPT_SSL_VERIFYHOST, 0);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
        $result = curl_exec($ch);
        if ($result === FALSE) {
            die('Curl failed: ' . curl_error($ch));
        }
        curl_close($ch);
        return $result;
    }

    // get appropriate rides from database
    public function getDrivers($regionPick, $regionDist){

        $sql = "SELECT * FROM `drivers` WHERE `region` LIKE '%$regionPick%' OR `region` LIKE '%$regionDist%'";

        $result = $this->conn->query($sql);

        return $result;

    }


    /**
     * Encrypting password
     * @param password
     * returns salt and encrypted password
     */
    public function hashSSHA($password) {

        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }

    /**
     * Decrypting password
     * @param salt, password
     * returns hash string
     */
    public function checkhashSSHA($salt, $password) {

        $hash = base64_encode(sha1($password . $salt, true) . $salt);

        return $hash;
    }
}

?>
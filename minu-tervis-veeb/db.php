<?php
$url = parse_url(getenv("CLEARDB_DATABASE_URL"));

$server = $url["host"];
$username = $url["user"];
$password = $url["pass"];
$db = substr($url["path"], 1);

$conn = new mysqli($server, $username, $password, $db);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$sql = "SET time_zone = '+2:00';";
if ($conn->query($sql) !== TRUE) {
    echo "Could not set time zone: " . mysqli_error($conn) . "<br>";
}
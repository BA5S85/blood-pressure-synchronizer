<?php

require("db.php");

// show doctor view
$sql = "SELECT * FROM vw_ArstiVaade ORDER BY Aeg DESC;";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    echo "<table class='table table-striped table-bordered table-hover'>";
    echo "<tr>";
    echo "<th>Patsiendi isikukood</th>";
    echo "<th>Patsiendi eesnimi</th>";
    echo "<th>Patsiendi perekonnanimi</th>";
    echo "<th>Mõõtmise kuupäev</th>";
    echo "<th>Süstoolne vererõhk</th>";
    echo "<th>Diastoolne vererõhk</th>";
    echo "<th>Pulss</th>";
    echo "</tr>";
    while ($row = $result->fetch_assoc()) {
        echo "<tr>";
        echo "<td>" . $row["Patsiendi_isikukood"] . "</td>";
        echo "<td>" . $row["Eesnimi"] . "</td>";
        echo "<td>" . $row["Perekonnanimi"] . "</td>";
        echo "<td>" . $row["Aeg"] . "</td>";
        echo "<td>" . $row["Systoolne"] . "</td>";
        echo "<td>" . $row["Diastoolne"] . "</td>";
        echo "<td>" . $row["Pulss"] . "</td>";
        echo "</tr>";
    }
    echo "</table>";
} else {
    die("'SELECT * FROM vw_ArstiVaade ORDER BY Aeg DESC;' query returned 0 rows");
}
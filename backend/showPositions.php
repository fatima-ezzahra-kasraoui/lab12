<?php
header('Content-Type: application/json; charset=utf-8');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    include_once __DIR__ . '/service/PositionService.php';
    $service = new PositionService();
    echo json_encode(["positions" => $service->getAll()]);
} else {
    http_response_code(405);
    echo json_encode(["ok" => false, "error" => "POST required"]);
}

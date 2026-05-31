<?php
include_once __DIR__ . '/../dao/IDao.php';
include_once __DIR__ . '/../classe/Position.php';
include_once __DIR__ . '/../connexion/Connexion.php';

class PositionService implements IDao {
    private $connexion;

    public function __construct() {
        $this->connexion = new Connexion();
    }

    public function create($position) {
        $sql  = "INSERT INTO position(latitude, longitude, date, imei) VALUES (?, ?, ?, ?)";
        $stmt = $this->connexion->getConnextion()->prepare($sql);
        $stmt->execute([
            $position->getLatitude(),
            $position->getLongitude(),
            $position->getDate(),
            $position->getImei()
        ]);
        return true;
    }

    public function getAll() {
        $stmt = $this->connexion->getConnextion()->prepare("SELECT * FROM position ORDER BY date DESC");
        $stmt->execute();
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function update($obj)   {}
    public function delete($obj)   {}
    public function getById($obj)  {}
}

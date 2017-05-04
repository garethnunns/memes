<?php
	require_once '../../site/functions.php';

	echo json_encode(star($_POST['key'],$_POST['id']));
?>
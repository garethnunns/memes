<?php
	require_once '../../site/functions.php';

	echo json_encode(login($_POST['username'],$_POST['password']));
?>
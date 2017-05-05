<?php
	require_once '../../site/functions.php';

	echo json_encode(setUserSurname($_POST['key'],$_POST['text']));
?>
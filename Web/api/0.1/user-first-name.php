<?php
	require_once '../../site/functions.php';

	echo json_encode(setUserFirstName($_POST['key'],$_POST['text']));
?>
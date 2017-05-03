<?php
	require_once '../../site/functions.php';

	echo json_encode(userDetailsPersonal($_POST['key'],$_POST['id']));
?>
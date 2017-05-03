<?php
	require_once '../../site/functions.php';

	echo json_encode(reposts($_POST['key'],$_POST['id'],$_POST['page']));
?>
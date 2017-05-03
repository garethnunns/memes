<?php
	require_once '../../site/functions.php';

	echo json_encode(stars($_POST['key'],$_POST['id'],$_POST['page']));
?>
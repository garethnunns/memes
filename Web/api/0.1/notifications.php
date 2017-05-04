<?php
	require_once '../../site/functions.php';

	echo json_encode(notifications($_POST['key'],$_POST['page'],$_POST['thumb']));
?>
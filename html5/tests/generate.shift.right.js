var data = [];
var curr = 0;
for (var i=0; i<1500; i++)
{
  var rnd = Math.round(Math.random() * 5); // Fork of 10 deg
  var sign = Math.round((Math.random() * 2)) % 2 == 0 ? 1 : -1;
  var twd = (i + (rnd * sign));
  while (twd > 360) twd -= 360;
  while (twd < 0) twd += 360;
  data.push(twd);
}
console.log(JSON.stringify(data));

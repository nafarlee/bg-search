const FIELDS = 'primary_name, year';

module.exports.name = (value, negate = false) => ({
  text: `SELECT ${FIELDS} FROM games WHERE primary_name ${negate ? '!' : ''}~~* $1`,
  values: [`%${value}%`],
});

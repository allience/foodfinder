using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Linq;
using MySql.Data.MySqlClient;

namespace FoodRecipesParser.Data
{
    public class DbConnect
    {
        private MySqlConnection _connection;
        private readonly string _server;
        private readonly string _database;
        private readonly string _uid;
        private readonly string _password;
        private string _connectionString;

        public DbConnect(string server, string database, string uid, string password)
        {
            _server = server;
            _database = database;
            _uid = uid;
            _password = password;
            Initialize();
        }

        public void Initialize()
        {
            _connectionString = $"SERVER={_server};DATABASE={_database};UID={_uid};PASSWORD={_password};";

            _connection = new MySqlConnection(_connectionString);
        }

        public bool OpenConnection()
        {
            try
            {
                _connection.Open();
                return true;
            }
            catch (MySqlException e)
            {
                switch (e.Number)
                {
                    case 0:
                        Console.WriteLine("Cannot connect to server.  Contact administrator");
                        break;

                    case 1045:
                        Console.WriteLine("Invalid username/password, please try again");
                        break;
                }
                return false;
            }
        }

        public bool CloseConnection()
        {
            try
            {
                _connection.Close();
                return true;
            }
            catch (MySqlException e)
            {
                Console.WriteLine(e.Message);
                return false;
            }
        }

        public Dictionary<string, string> GetById(string table, int id)
        {
            var records = WhereAll(table, new List<Tuple<string, string, object>>
            {
                new Tuple<string, string, object>("id", "=", id)
            });

            return records.FirstOrDefault();
        }

        public List<Dictionary<string, string>> WhereAll(string table, List<Tuple<string, string, object>> conditions)
        {
            var data = new List<Dictionary<string, string>>();

            var query = conditions.Aggregate($"SELECT * FROM {table} WHERE ", (current, tuple) =>
                current + BuildWhereClause(conditions, tuple, "AND"));
            query += ";";

            var schema = GetSchema(table);

            if (!OpenConnection()) return null;

            try
            {
                using (var cmd = new MySqlCommand(query, _connection))
                {
                    using (var dataReader = cmd.ExecuteReader())
                    {
                        while (dataReader.Read())
                        {
                            var record = new Dictionary<string, string>();
                            foreach (
                                var colName in from DataRow col in schema.Rows select col.Field<string>("ColumnName"))
                            {
                                record[colName] = dataReader[colName].ToString();
                            }
                            data.Add(record);
                        }
                    }
                }
            }
            catch (MySqlException e)
            {
                Console.WriteLine("----------------------------------------------");
                Console.WriteLine($"TABLE: {table}");
                Console.WriteLine(e.Message);
                Console.WriteLine("----------------------------------------------");
            }
            finally
            {
                CloseConnection();
            }

            return data;
        }

        private string BuildWhereClause(List<Tuple<string, string, object>> conditions,
            Tuple<string, string, object> tuple, string logOp)
        {
            if (tuple.Equals(conditions[0]))
                return $"`{tuple.Item1}`{tuple.Item2}'{tuple.Item3}'";
            if (tuple.Equals(conditions[conditions.Count - 1]))
                return $" {logOp} `{tuple.Item1}`{tuple.Item2}'{tuple.Item3}'";
            return $" {logOp} `{tuple.Item1}`{tuple.Item2}'{tuple.Item3}'";
        }

        public void Insert(string table, IList<object> values)
        {
            var schema = GetSchema(table);
        
            var query = schema.Rows.Cast<DataRow>()
                .Aggregate($"INSERT INTO {table} (",
                    (current, col) =>
                        current + ((!(bool)col.ItemArray[17]) ? // ItemArray[17] = auto_increment
                        (col != schema.Rows[schema.Rows.Count - 1]
                            ? $"`{col.Field<string>("ColumnName")}`, "
                            : $"`{col.Field<string>("ColumnName")}`) VALUES(") : ""));

            query = values.Aggregate(query, (current, val) => current + (val != values.Last() ? $"'{val?.ToString().Replace("'", "''")}', " : $"'{val?.ToString().Replace("'", "''")}')"));

            try
            {
                using (var cmd = new MySqlCommand(query, _connection))
                {
                    if (!OpenConnection()) return;
                    cmd.ExecuteNonQuery();
                }
            }
            catch (MySqlException e)
            {
                using (var file = new StreamWriter(@"log.txt", true))
                {
                    file.WriteLine("----------------------------------------------");
                    file.WriteLine($"TABLE: {table}");
                    file.WriteLine(e.Message);
                    file.WriteLine("----------------------------------------------");
                }
            }
            finally
            {
                CloseConnection();
            }
        }

        private DataTable GetSchema(string table)
        {
            DataTable schema;
            using (var cmd = new MySqlCommand($"SELECT * FROM {table}", _connection))
            {
                if (!OpenConnection()) return null;

                using (var reader = cmd.ExecuteReader(CommandBehavior.SchemaOnly))
                {
                    schema = reader.GetSchemaTable();
                }

                CloseConnection();
            }

            return schema;
        }
    }
}
